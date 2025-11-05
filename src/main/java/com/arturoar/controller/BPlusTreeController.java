package com.arturoar.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.arturoar.model.*;
import com.arturoar.ui.BPlusTreeUI;
import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.TreeAnimator;
import com.arturoar.util.TreeViewTransformer;
import com.arturoar.view.TreeView;

import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Label;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;

public class BPlusTreeController implements Initializable {

    private static final int DEFAULT_BRANCHING_FACTOR = 5;


    @FXML private AnchorPane rootPane;
    @FXML private Pane canvas;
    @FXML private StackPane settingsSP;
    @FXML private StackPane searchSP;
    @FXML private StackPane insertSP;
    @FXML private StackPane deleteSP;
    @FXML private MFXFontIcon insertIcon;
    @FXML private MFXFontIcon removeIcon;
    @FXML private MFXFontIcon searchIcon;
    @FXML private MFXFontIcon homeIcon;
    @FXML private MFXFontIcon settingsIcon;
    @FXML private MFXFontIcon minimizeBtn;
    @FXML private MFXFontIcon maximizeBtn;
    @FXML private MFXFontIcon closeBtn;
    @FXML private VBox searchBtnBox;
    @FXML private VBox insertBtnBox;
    @FXML private VBox removeBtnBox;
    @FXML private VBox homeBtnBox;
    @FXML private VBox settingsBtnBox;
    @FXML private HBox headerBox;
    @FXML private HBox controlBar;
    @FXML private Pane dialogOverlay;
    @FXML private Label messageLabel;
    @FXML private Label treeInfoLabel;


    

    private BooleanProperty themeMode = new SimpleBooleanProperty();
    private DoubleProperty animationSpeed = new SimpleDoubleProperty(1.0);
    private DoubleProperty mParameter = new SimpleDoubleProperty();
    private BooleanProperty traversalAnimation = new SimpleBooleanProperty();

    private final ExecutorService animationExecutor = Executors.newSingleThreadExecutor();



    private int branchingFactor = DEFAULT_BRANCHING_FACTOR;

    // BPlusTree model instance
    private final BPlusTree<Integer, String> tree;
    private TreeViewTransformer transformer;
    
    private TreeView treeView;


    private double xOffset = 0;
    private double yOffset = 0;

    private BooleanProperty isDialogActive = new SimpleBooleanProperty(false);

    private Stage stage;
    private MFXGenericDialog dialogContent;
    private MFXStageDialog dialog;
    

    public BPlusTreeController(Stage stage) {
        this.stage = stage;
        this.tree = new BPlusTree<>(this.branchingFactor);
        this.treeView = new TreeView(this.tree.getRoot());
        this.tree.addObserver(this.treeView);
        TreeAnimator.getInstance().getAnimationSpeedProperty().bind(this.animationSpeed);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWindowControls();
        setupResponsiveLayout();
        makeStageDraggable();
        setupButtonActions();
        setupCanvas();
        Platform.runLater(() -> {
            setupPopupPositions();
        });
        loadDialogs();

        ChangeListener<Boolean> dialogActiveListener = (_,_, isActive ) -> {
            if (isActive){
                controlBar.getStyleClass().add("lightup");
                dialogOverlay.setVisible(true);
                
            }else{
                controlBar.getStyleClass().remove("lightup");
                dialogOverlay.setVisible(false);
            }
        };

        isDialogActive.addListener(dialogActiveListener);
        
        this.transformer = new TreeViewTransformer(canvas, treeView);

        themeMode.addListener((_,_,newVal)->{
            if (newVal){
                rootPane.getStyleClass().remove("light");
                rootPane.getStyleClass().add("dark");
                
            }else{
                rootPane.getStyleClass().remove("dark");
                rootPane.getStyleClass().add("light");
            }
        });

        messageLabel.setText("Empty Tree");
        treeInfoLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                String.format("Tree m parameter: %d\nTree depth: %d\n" + //
                                        "Tree width: %d", 
                    (int)mParameter.get(),
                    treeView.depthProperty().get(), 
                    treeView.widthProperty().get()
                ), mParameter, treeView.depthProperty(), treeView.widthProperty()
        ));
        setupDialog();
    }

    private void setupWindowControls() {
        closeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> Platform.exit());
		minimizeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, _ ->  ((Stage) rootPane.getScene().getWindow()).setIconified(true));
		maximizeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> { ((Stage) rootPane.getScene().getWindow()).setMaximized( !((Stage) rootPane.getScene().getWindow()).isMaximized() );});
    }

    private void loadDialogs() {
        try {
            FXMLLoader loader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/SettingsDialog.fxml"));
            Parent settingsContent = loader.load();

            themeMode.bind(((SettingsDialogController)loader.getController()).themeToggleProperty());
            animationSpeed.bind(((SettingsDialogController)loader.getController()).speedSliderValueProperty());
            mParameter.bind(((SettingsDialogController)loader.getController()).mProperty());
            traversalAnimation.bind(((SettingsDialogController)loader.getController()).traversalToggleProperty());
            
            settingsSP.getChildren().clear();
            settingsSP.getChildren().add(settingsContent);
            settingsSP.setVisible(false); 

            FXMLLoader insertLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/InsertDialog.fxml"));
            Parent insertContent = insertLoader.load();
            ((InsertDialogController)insertLoader.getController()).setOnCancelCallback(this::closeAllDialogs);
            ((InsertDialogController)insertLoader.getController()).setOnInsertCallback(pair -> handleInsert(pair.getKey(), pair.getValue()));


            insertSP.getChildren().clear();
            insertSP.getChildren().add(insertContent);
            insertSP.setVisible(false); 
            
            FXMLLoader removeLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/RemoveDialog.fxml"));
            Parent removeContent = removeLoader.load();
            ((RemoveDialogController)removeLoader.getController()).setOnCancelCallback(this::closeAllDialogs);
            ((RemoveDialogController)removeLoader.getController()).setOnRemoveCallback(key -> handleRemove(key));
            
            deleteSP.getChildren().clear();
            deleteSP.getChildren().add(removeContent);
            deleteSP.setVisible(false); 
            
            FXMLLoader searchLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/SearchDialog.fxml"));
            Parent searchContent = searchLoader.load();
            ((SearchDialogController)searchLoader.getController()).setOnCancelCallback(this::closeAllDialogs);
            ((SearchDialogController)searchLoader.getController()).setOnSearchCallback(key -> handleSearch(key));
            
            searchSP.getChildren().clear();
            searchSP.getChildren().add(searchContent);
            searchSP.setVisible(false); 
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleSettingsDialog() {
        boolean isVisible = settingsSP.isVisible();
        settingsSP.setVisible(!isVisible);
        
        // Ocultar otros popups si están visibles
        searchSP.setVisible(false);
        insertSP.setVisible(false);
        deleteSP.setVisible(false);
        
        isDialogActive.set(!isVisible);
    }

    private void toggleInsertDialog() {
        boolean isVisible = insertSP.isVisible();
        insertSP.setVisible(!isVisible);
        
        // Ocultar otros popups si están visibles
        searchSP.setVisible(false);
        settingsSP.setVisible(false);
        deleteSP.setVisible(false);
        isDialogActive.set(!isVisible);
    }

    private void toggleDeleteDialog() {
        boolean isVisible = deleteSP.isVisible();
        deleteSP.setVisible(!isVisible);
        
        // Ocultar otros popups si están visibles
        searchSP.setVisible(false);
        settingsSP.setVisible(false);
        insertSP.setVisible(false);
        isDialogActive.set(!isVisible);
    }

    private void toggleSearchDialog() {
        boolean isVisible = searchSP.isVisible();
        searchSP.setVisible(!isVisible);
        
        // Ocultar otros popups si están visibles
        insertSP.setVisible(false);
        settingsSP.setVisible(false);
        deleteSP.setVisible(false);
        isDialogActive.set(!isVisible);
    }

    private void closeAllDialogs(){
        
        insertSP.setVisible(false);
        settingsSP.setVisible(false);
        deleteSP.setVisible(false);
        searchSP.setVisible(false);   
        
        isDialogActive.set(false);
    }


    private void setupResponsiveLayout() {
        // Header - anclado arriba
        AnchorPane.setTopAnchor(headerBox, 0.0);
        AnchorPane.setLeftAnchor(headerBox, 0.0);
        AnchorPane.setRightAnchor(headerBox, 0.0);
        
        // Control bar - posición fija desde arriba
        AnchorPane.setTopAnchor(controlBar, 67.0);
        AnchorPane.setLeftAnchor(controlBar, 0.0);
        AnchorPane.setRightAnchor(controlBar, 0.0);
        
        // Canvas - ocupa espacio restante
        
        AnchorPane.setTopAnchor(canvas, 159.0);
        AnchorPane.setBottomAnchor(canvas, 11.0);
        AnchorPane.setLeftAnchor(canvas, 10.0);
        AnchorPane.setRightAnchor(canvas, 10.0);


        AnchorPane.setBottomAnchor(dialogOverlay, 0.0);
        AnchorPane.setTopAnchor(dialogOverlay, 0.0);
        AnchorPane.setLeftAnchor(dialogOverlay, 0.0);
        AnchorPane.setRightAnchor(dialogOverlay, 0.0);

    }

    private void setupPopupPositions() {
        ChangeListener<Number> positionListener = (_, _, _) -> updatePopupPositions();

        searchBtnBox.layoutXProperty().addListener(positionListener);
        insertBtnBox.layoutXProperty().addListener(positionListener);
        removeBtnBox.layoutXProperty().addListener(positionListener);
        settingsBtnBox.layoutXProperty().addListener(positionListener);

        updatePopupPositions();
    
    }

    private void updatePopupPositions() {

        AnchorPane.setTopAnchor(searchSP, 159.0);
        AnchorPane.setLeftAnchor(searchSP, searchBtnBox.getLayoutX()+ searchBtnBox.getWidth()/2-(searchSP.getPrefWidth()/2));
        
        AnchorPane.setTopAnchor(insertSP, 159.0);
        AnchorPane.setLeftAnchor(insertSP, insertBtnBox.getLayoutX()+ insertBtnBox.getWidth()/2-(insertSP.getPrefWidth()/2));

        AnchorPane.setTopAnchor(deleteSP, 159.0);
        AnchorPane.setLeftAnchor(deleteSP, removeBtnBox.getLayoutX()+ removeBtnBox.getWidth()/2-(deleteSP.getPrefWidth()/2));

        AnchorPane.setTopAnchor(settingsSP, 159.0);
        AnchorPane.setLeftAnchor(settingsSP, settingsBtnBox.getLayoutX()+ settingsBtnBox.getWidth()/2-(settingsSP.getPrefWidth()/2));

        AnchorPane.setTopAnchor(messageLabel, rootPane.getHeight()/2);
        AnchorPane.setLeftAnchor(messageLabel, (rootPane.getWidth()/2) - (messageLabel.getWidth()/2));

        AnchorPane.setTopAnchor(treeInfoLabel, 170.0);
        AnchorPane.setLeftAnchor(treeInfoLabel, 20.0);



    }

    private void setupDialog(){

        Platform.runLater(() -> {
            this.dialogContent = MFXGenericDialogBuilder.build()
                    .setContentText("The current Tree would reset. Are you sure you wnat to continue?")
                    .makeScrollable(true)
                    .get();
                    
            this.dialog = MFXGenericDialogBuilder.build(dialogContent)
                    .toStageDialogBuilder()
                    .initOwner(stage)
                    .initModality(Modality.WINDOW_MODAL)
                    .setDraggable(true)
                    .setTitle("Dialogs Preview")
                    .setOwnerNode(rootPane)
                    .setScrimPriority(ScrimPriority.WINDOW)
                    .setScrimOwner(true)
                    .get();

            dialogContent.addActions(
                    Map.entry(new MFXButton("Confirm"), event -> {
                        // Acción de confirmar
                        dialog.close();
                    }),
                    Map.entry(new MFXButton("Cancel"), event -> dialog.close())
            );
        });	
    }

    public void showDialog() {
        Platform.runLater(() -> {
            if (dialog != null) {
                dialog.showDialog(); // Modal - bloquea la ventana padre
                // dialog.show();    // No modal - no bloquea
            }
        });
    }

    // Método para cerrar
    public void closeDialog() {
        Platform.runLater(() -> {
            if (dialog != null) {
                dialog.close();
            }
        });
    }

    private void setupCanvas() {

        // Crear un Rectangle para el clip
        Rectangle clip = new Rectangle();
        
        // Bind del clip al tamaño del Pane
        clip.widthProperty().bind(canvas.widthProperty());
        clip.heightProperty().bind(canvas.heightProperty());
        
        // Aplicar el clip al Pane
        canvas.setClip(clip);

        canvas.getChildren().add(treeView);
        treeView.canvasHeightProperty().bind(this.canvas.heightProperty());
        treeView.canvasWidthProperty().bind(this.canvas.widthProperty());

    }

    private void setupButtonActions() {
        dialogOverlay.setVisible(false);
        homeBtnBox.setOnMouseClicked(_ -> transformer.resetView());
        insertBtnBox.setOnMouseClicked(_ -> toggleInsertDialog());
        removeBtnBox.setOnMouseClicked(_ -> toggleDeleteDialog());
        searchBtnBox.setOnMouseClicked(_ -> toggleSearchDialog());
        settingsBtnBox.setOnMouseClicked(_ -> toggleSettingsDialog());
        dialogOverlay.setOnMouseClicked(_ ->  closeAllDialogs());
    }
    
    private void handleInsert(Integer key, String data) {
        
        
        BPlusTraversalResult<Boolean, Integer, String> result = this.tree.insert(key, data);
        
        //disableButtons();
        if (traversalAnimation.get()){
            treeView.animateTraversal(result.getVisitedKeys());
        }
        
        animationExecutor.submit(() -> {
            try {
                Thread.sleep(100); // Pequeño delay para MaterialFX
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    
            

            if (!result.getResult()){
                messageLabel.setText("Key already on the Tree");
                messageLabel.getStyleClass().add("warning");
                TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(messageLabel, 0.0, 1.0));
                TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(messageLabel, 1.0, 0.0));
            }

            //TreeAnimator.getInstance().addListenerToLastTransition( () -> enableButtons());
            TreeAnimator.getInstance().animateQueue();
            //Platform.runLater(() -> {});
        });
    }

    private void handleRemove(Integer key) {

        BPlusTraversalResult<Key<Integer>, Integer, String> result = this.tree.remove(key);
        
        if (traversalAnimation.get()){
            treeView.animateTraversal(result.getVisitedKeys());
        }        

        disableButtons();

        if (result.getResult() == null) {
            messageLabel.setText("Key not found");
            messageLabel.getStyleClass().add("warning");
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(messageLabel, 0.0, 1.0));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(messageLabel, 1.0, 0.0));
        }

        TreeAnimator.getInstance().addListenerToLastTransition(() -> enableButtons());
        
        Platform.runLater(() -> {
            TreeAnimator.getInstance().animateQueue();
        });




    }

    private void handleSearch(Integer key) {

        
        BPlusTraversalResult<BPlusLeafNode<Integer, String>, Integer, String>  result =  this.tree.search(key);
        
        if (traversalAnimation.get()){
            treeView.animateTraversal(result.getVisitedKeys());
        }        

        disableButtons();

        if (result.getResult() == null) {
            messageLabel.setText("Key not found");
            messageLabel.getStyleClass().add("warning");
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(messageLabel, 0.0, 1.0));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(messageLabel, 1.0, 0.0));
        }

        TreeAnimator.getInstance().addListenerToLastTransition(() -> enableButtons());
        
        Platform.runLater(() -> {
            TreeAnimator.getInstance().animateQueue();
        });

    }

    private void disableButtons() {
        this.insertBtnBox.setDisable(true);
        this.removeBtnBox.setDisable(true);
        this.searchBtnBox.setDisable(true);
        this.homeBtnBox.setDisable(true);
    }

    private void enableButtons() {
        this.insertBtnBox.setDisable(false);
        this.removeBtnBox.setDisable(false);
        this.searchBtnBox.setDisable(false);
        this.homeBtnBox.setDisable(false);
        this.treeView.updateKeyViews();
    }


    private void makeStageDraggable() {
    headerBox.setOnMousePressed(event -> {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    });
    
    headerBox.setOnMouseDragged(event -> {
        Stage stage = (Stage) headerBox.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    });
}
}
