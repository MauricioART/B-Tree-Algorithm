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
import com.arturoar.view.KeyView;
import com.arturoar.view.TreeView;

import org.kordamp.ikonli.coreui.CoreUiFree;

import javafx.animation.FadeTransition;
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
    @FXML private Label treeInfoLabel;

    
    
    private InfoMessage notFound;
    private InfoMessage emptyTree;
    private SettingsDialogController settingsController;
    private InsertDialogController insertDialogController;
    private RemoveDialogController removeDialogController;
    private SearchDialogController searchDialogController;


    private BooleanProperty darkMode = new SimpleBooleanProperty();
    private DoubleProperty animationSpeed = new SimpleDoubleProperty(1.0);
    private DoubleProperty mParameter = new SimpleDoubleProperty();
    private BooleanProperty traversalAnimation = new SimpleBooleanProperty();
    private BooleanProperty isTreeEmptyProperty = new SimpleBooleanProperty();


    private int m;

    private final ExecutorService animationExecutor = Executors.newSingleThreadExecutor();


    // BPlusTree model instance
    private BPlusTree<Integer, String> tree;
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
        TreeAnimator.getInstance().getAnimationSpeedProperty().bind(this.animationSpeed);
    }

    private void initializeTree(){
        
        if(this.tree != null){
            this.canvas.getChildren().clear();
        }
        tree = new BPlusTree<>(m);
        treeView = new TreeView();
        tree.addObserver(this.treeView);
        transformer = new TreeViewTransformer(canvas, treeView);

        treeView.darkModeProperty().bind(darkMode);

        treeInfoLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                String.format("Tree m parameter: %d\nTree depth: %d\n" + //
                                        "Tree width: %d", 
                    (int)mParameter.get(),
                    treeView.depthProperty().get(), 
                    treeView.widthProperty().get()
                ), mParameter, treeView.depthProperty(), treeView.widthProperty()
        ));

        isTreeEmptyProperty.bind(tree.emptyProperty());
        
        canvas.getChildren().add(treeView);
        treeView.canvasHeightProperty().bind(this.canvas.heightProperty());
        treeView.canvasWidthProperty().bind(this.canvas.widthProperty());
        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWindowControls();
        setupResponsiveLayout();
        makeStageDraggable();
        setupButtonActions();
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
        
        
        
        darkMode.addListener((_,_,newVal)->{
            if (newVal){
                rootPane.getStyleClass().add("dark");
                
            }else{
                rootPane.getStyleClass().remove("dark");
            }
        });
        
        setupDialog();
        setupCanvas();
        m = (int)mParameter.get();
        initializeTree();

        isTreeEmptyProperty.addListener((_,_,newVal)->{
            FadeTransition transition = new FadeTransition(Duration.millis(1000),emptyTree);
            if (newVal){
                transition.setFromValue(0.0);
                transition.setToValue(1.0);
                TreeAnimator.getInstance().addListenerToLastTransition(()->{
                    
                    transition.play();
                });
                
            }else{
                transition.setFromValue(1.0);
                transition.setToValue(0.0);
                transition.play();
            }
        });
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
            settingsController = (SettingsDialogController)loader.getController();

            darkMode.bind(settingsController.themeToggleProperty());animationSpeed.bind(Bindings.createDoubleBinding(() -> {
                double speedValue = settingsController.speedSliderValueProperty().get();
                return speedValue != 0 ? 1.0 / speedValue : 1.0; 
            }, settingsController.speedSliderValueProperty()));
            mParameter.bind(settingsController.mProperty());
            traversalAnimation.bind(settingsController.traversalToggleProperty());
            
            ((SettingsDialogController)loader.getController()).setOnMSliderChange(()->{
                if (!tree.emptyProperty().get()){
                    showDialog();
                }
            });

            settingsSP.getChildren().clear();
            settingsSP.getChildren().add(settingsContent);
            settingsSP.setVisible(false); 
           

            FXMLLoader insertLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/InsertDialog.fxml"));
            Parent insertContent = insertLoader.load();
            insertDialogController = (InsertDialogController)insertLoader.getController();
            insertDialogController.setOnCancelCallback(this::closeAllDialogs);
            insertDialogController.setOnInsertCallback(pair -> handleInsert(pair.getKey(), pair.getValue()));

            insertSP.getChildren().clear();
            insertSP.getChildren().add(insertContent);
            insertSP.setVisible(false); 

            
            FXMLLoader removeLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/RemoveDialog.fxml"));
            Parent removeContent = removeLoader.load();
            removeDialogController = (RemoveDialogController)removeLoader.getController();
            removeDialogController.setOnCancelCallback(this::closeAllDialogs);
            removeDialogController.setOnRemoveCallback(key -> handleRemove(key));
            
            deleteSP.getChildren().clear();
            deleteSP.getChildren().add(removeContent);
            deleteSP.setVisible(false); 
            
            FXMLLoader searchLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/SearchDialog.fxml"));
            Parent searchContent = searchLoader.load();
            searchDialogController = (SearchDialogController)searchLoader.getController();
            searchDialogController.setOnCancelCallback(this::closeAllDialogs);
            searchDialogController.setOnSearchCallback(key -> handleSearch(key));
            
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

        AnchorPane.setTopAnchor(treeInfoLabel, 170.0);
        AnchorPane.setLeftAnchor(treeInfoLabel, 20.0);



    }

    private void setupDialog() {
    Platform.runLater(() -> {
        // Crear icono de advertencia para el header
        MFXFontIcon warningIcon = new MFXFontIcon("fas-exclamation-triangle", 48);
        warningIcon.getStyleClass().add("warning-icon");

        this.dialogContent = MFXGenericDialogBuilder.build()
                .setHeaderIcon(warningIcon)
                .setHeaderText("Warning")
                .setContentText("The current Tree would reset. Are you sure you want to continue?")
                .makeScrollable(true)
                .get();

        this.dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(stage)
                .initModality(Modality.WINDOW_MODAL)
                .setDraggable(true)
                .setTitle("Reset Tree Confirmation")
                .setOwnerNode(rootPane)
                .setScrimPriority(ScrimPriority.WINDOW)
                .setScrimOwner(true)
                .get();

        dialogContent.addActions(
                Map.entry(new MFXButton("Confirm"), event -> {
                    m = (int) mParameter.get();
                    isTreeEmptyProperty.unbind();
                    initializeTree();
                    closeAllDialogs();
                    dialog.close();
                }),
                Map.entry(new MFXButton("Cancel"), event -> {
                    settingsController.getMSlider().setValue(m);
                    dialog.close();
                })
        );
    });
}
    public void showDialog() {
        Platform.runLater(() -> {
            if (dialog != null) {
                dialog.showDialog(); 

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
        
        emptyTree = new InfoMessage();
        emptyTree.setIcon("fas-sitemap");
        emptyTree.setMessage("Empty tree");
        emptyTree.getStyleClass().add("message");

        emptyTree.layoutXProperty().bind(
            canvas.widthProperty().subtract(emptyTree.widthProperty()).divide(2)
        );
        emptyTree.layoutYProperty().bind(
            canvas.heightProperty().subtract(emptyTree.heightProperty()).divide(2).subtract(100)
        );
        canvas.getChildren().add(emptyTree);


        notFound = new InfoMessage();
        notFound.setIcon("fas-mitten");
        notFound.setMessage("Key not Found!");

        notFound.layoutXProperty().bind(
           canvas.widthProperty().subtract(notFound.widthProperty()).divide(2)
        );
        notFound.layoutYProperty().bind(
            canvas.heightProperty().subtract(notFound.heightProperty()).divide(2)
        );

        canvas.getChildren().add(notFound);
        notFound.setOpacity(0.0);

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

        
        disableButtons();
        BPlusTraversalResult<Boolean, Integer, String> result = this.tree.insert(key, data);
        if (traversalAnimation.get()){
            treeView.animateTraversal(result.getVisitedKeys());
        }


        if (!result.getResult()){
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(notFound, 0.0, 1.0));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().pauseTransition(1300));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(notFound, 1.0, 0.0));
        }
        animationExecutor.submit(() -> {
           
            TreeAnimator.getInstance().addListenerToLastTransition( () ->{
                Platform.runLater(()->{
                    enableButtons();
                    if(!result.getResult()){        
                        canvas.getChildren().remove(notFound);
                        canvas.getChildren().add(notFound);    
                        notFound.setIcon("far-frown-open");
                        notFound.setMessage("Key already on the Tree");
                        notFound.getStyleClass().add("warning");
                    }
                });
            });

            Platform.runLater(() -> {
                    TreeAnimator.getInstance().animateQueue(treeView.allowTranslationProperty());
            });
            
        });
    }

    private void handleRemove(Integer key) {

        BPlusTraversalResult<Key<Integer>, Integer, String> result = this.tree.remove(key);
        
        if (traversalAnimation.get()){
            treeView.animateTraversal(result.getVisitedKeys());
        }        

        disableButtons();

        if (result.getResult() == null) {
            notFound.setMessage("Key not found");
            notFound.getStyleClass().add("warning");
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(notFound, 0.0, 1.0));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().pauseTransition(1000));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(notFound, 1.0, 0.0));
        }
        


        animationExecutor.submit(() -> {
           
            TreeAnimator.getInstance().addListenerToLastTransition( () ->{
                Platform.runLater(()->{
                    canvas.getChildren().remove(notFound);
                    canvas.getChildren().add(notFound);   
                    enableButtons();
                });
            });

            Platform.runLater(() -> {
                    TreeAnimator.getInstance().animateQueue(treeView.allowTranslationProperty());
            });
            
        });




    }

    private void handleSearch(Integer key) {

        
        BPlusTraversalResult<BPlusLeafNode<Integer, String>, Integer, String>  result =  this.tree.search(key);
        
        if (result.getResult() == null) {
            notFound.setMessage("Key not found");
            notFound.getStyleClass().add("warning");
            
                canvas.getChildren().remove(notFound);
                canvas.getChildren().add(notFound);   
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(notFound, 0.0, 1.0));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().pauseTransition(1000));
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().fadeNode(notFound, 1.0, 0.0));
        }else{
            KeyView searchedKey = this.treeView.getKeyView( result.getVisitedKeys().getLast());
            TreeAnimator.getInstance().addTransitionToQueue(TreeAnimator.getInstance().highlightData(searchedKey.getData()));
        }

        if (traversalAnimation.get()){
            treeView.animateTraversal(result.getVisitedKeys());
        }        

        disableButtons();

      
        TreeAnimator.getInstance().addListenerToLastTransition( () ->{
                enableButtons();
        });

        Platform.runLater(() -> {
                TreeAnimator.getInstance().animateQueue(treeView.allowTranslationProperty());
        });
        
        
     //   animationExecutor.submit(() -> { });



    }

    private void disableButtons() {
        this.insertBtnBox.setDisable(true);
        this.removeBtnBox.setDisable(true);
        this.searchBtnBox.setDisable(true);
        this.homeBtnBox.setDisable(true);
        this.settingsBtnBox.setDisable(true);
    }

    private void enableButtons() {
        this.insertBtnBox.setDisable(false);
        this.removeBtnBox.setDisable(false);
        this.searchBtnBox.setDisable(false);
        this.homeBtnBox.setDisable(false);
        this.settingsBtnBox.setDisable(false);
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
