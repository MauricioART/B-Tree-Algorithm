package com.arturoar.controller;

import java.util.ResourceBundle;

import com.arturoar.model.*;
import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.TreeAnimator;
import com.arturoar.util.TreeViewTransformer;
import com.arturoar.view.TreeView;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import io.github.palexdev.mfxresources.fonts.IconDescriptor;
import io.github.palexdev.mfxresources.fonts.IconsProviders;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeBrands;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeRegular;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeSolid;

import java.net.URL;


public class BPlusTreeController implements Initializable{

    private static final int DEFAULT_BRANCHING_FACTOR = 4;


    @FXML private AnchorPane rootPane;
    @FXML private Pane canvas;
    @FXML private MFXFontIcon insertBtn;
    @FXML private MFXFontIcon removeBtn;
    @FXML private MFXFontIcon searchBtn;
    @FXML private MFXFontIcon homeBtn;
    @FXML private MFXFontIcon minimizeBtn;
    @FXML private MFXFontIcon maximizeBtn;
    @FXML private MFXFontIcon closeBtn;
    //@FXML private Slider speedSlider;
    //@FXML private Button themeModeBtn;
    //@FXML private Label speedLabel;
    //@FXML private AnchorPane anchorPane;
    
    private int branchingFactor = DEFAULT_BRANCHING_FACTOR;

    // BPlusTree model instance
    private final BPlusTree<Integer, String> tree;
    private TreeViewTransformer transformer;
    
    private TreeView treeView;

    private DoubleProperty animationSpeed = new SimpleDoubleProperty(1.0);
    

    public BPlusTreeController() {
        this.tree = new BPlusTree<>(this.branchingFactor);
        this.treeView = new TreeView(this.tree.getRoot());
        this.tree.addObserver(this.treeView);
        TreeAnimator.getInstance().getAnimationSpeedProperty().bind(this.animationSpeed);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupIcons();
        closeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> Platform.exit());
		minimizeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> ((Stage) rootPane.getScene().getWindow()).setIconified(true));
		maximizeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> { ((Stage) rootPane.getScene().getWindow()).setMaximized( !((Stage) rootPane.getScene().getWindow()).isMaximized() );});

        setupCanvas();
        this.transformer = new TreeViewTransformer(canvas, treeView);
        addStyleClasses();
        setupButtonActions();
        //setupSpeedSlider();
    }

    private void setupIcons(){
        // Use FontAwesomeSolid enum constants directly (they implement IconDescriptor)
        
        
        /*      
        this.homeBtn.setDescription("fa-etch fa-solid fa-house");
        this.insertBtn.setDescription("fa-etch fa-solid fa-plus-circle"); 
        this.removeBtn.setDescription("fa-etch fa-solid fa-minus-circle");
        this.searchBtn.setDescription("fa-etch fa-solid fa-magnifying-glass");
        this.closeBtn.setDescription("fa-etch fa-solid fa-times-circle");
        this.maximizeBtn.setDescription("fa-etch fa-solid fa-window-maximize");
        this.minimizeBtn.setDescription("fa-etch fa-solid fa-window-minimize");
        this.homeBtn.setIconDescriptor(FontAwesomeSolid.HOME);
        this.insertBtn.setIconDescriptor(FontAwesomeSolid.PLUS_CIRCLE);
        this.removeBtn.setIconDescriptor(FontAwesomeSolid.MINUS_CIRCLE);
        this.searchBtn.setIconDescriptor(FontAwesomeSolid.SEARCH);
        this.closeBtn.setIconDescriptor(FontAwesomeSolid.TIMES_CIRCLE);
        this.maximizeBtn.setIconDescriptor(FontAwesomeSolid.WINDOW_MAXIMIZE);
        this.minimizeBtn.setIconDescriptor(FontAwesomeSolid.WINDOW_MINIMIZE); */
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

        canvas.setOnMouseClicked(_->{
            rootPane.getStyleClass().remove("rootPane"); 
            rootPane.getStyleClass().add("light");
            System.out.println("◆◆◆◆◆ Modo claro activado ◆◆◆◆◆");
        });

    }

    private void addStyleClasses() {
        rootPane.getStyleClass().add("light");
    }

    private void setupButtonActions() {
        
        //MFXFontIcon icon = new MFXFontIcon(FontAwesomeSolid.HOUSE.getDescription(), 16);
        //this.homeBtn.setGraphic(icon);

        this.homeBtn.setOnMouseClicked(_ -> transformer.resetView());
        this.insertBtn.setOnMouseClicked(_-> handleInsert());
        this.removeBtn.setOnMouseClicked(_-> handleRemove());
        this.searchBtn.setOnMouseClicked(_-> handleSearch());

        this.rootPane.setOnMouseClicked(_ -> {
            
            rootPane.getStyleClass().remove("rootPane"); 
            rootPane.getStyleClass().add("light");
            System.out.println("◆◆◆◆◆ Modo claro activado ◆◆◆◆◆");
        });
    }

    
    /*private void setupSpeedSlider() {
        this.speedSlider.setBlockIncrement(0.25);
        this.speedSlider.setValue(1.0);
        this.speedSlider.setMin(0.25);
        this.speedSlider.setMax(1.5);
        this.speedSlider.valueProperty().addListener((_, _, newVal) -> {

            animationSpeed.set(1/newVal.doubleValue());
        });
        this.speedLabel.textProperty().bind(
            animationSpeed.asString("Speed: %.2fx")
        );
    }*/
    
    private void handleInsert() {
        Dialog<String[]> dialog = createKeyValueDialog("New Node", "Insert the new key:", true);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (!insert(key, values[1])) {
                    showAlert(Alert.AlertType.WARNING, "Insertion Error", "Key already exists");
                }
                this.tree.showTree();
                disableButtons();
                TreeAnimator.getInstance().addListenerToLastTransition(() -> enableButtons());

                TreeAnimator.getInstance().animateQueue();
            } catch (NumberFormatException e) {
                showInputErrorAlert("Please enter a valid integer key.");
            }
        });
    }

    private void handleRemove() {
        Dialog<String[]> dialog = createKeyValueDialog("Remove Node", "Insert the key:", false);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (remove(key) == null) {
                    showAlert(Alert.AlertType.ERROR, "Removal Error", 
                            "The structure does not contain key " + key);
                }
                disableButtons();                
                TreeAnimator.getInstance().addListenerToLastTransition(() -> enableButtons());
                this.tree.showTree();
                TreeAnimator.getInstance().animateQueue();
            } catch (NumberFormatException e) {
                showInputErrorAlert("Please enter a valid integer key.");
            }
        });
    }

    private void handleSearch() {
        Dialog<String[]> dialog = createKeyValueDialog("Searching Node", "Insert the key:", false);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                BPlusLeafNode<Integer, String> result = search(key);
                if ( result == null) {
                    showAlert(Alert.AlertType.ERROR, "Search Error", 
                            "The structure does not contain key " + key);
                }else{
                    // TODOw: Implement search animation
                    System.out.println("◆◆◆◆◆ Búsqueda exitosa ◆◆◆◆◆");
                }
                disableButtons();
                TreeAnimator.getInstance().addListenerToLastTransition(() -> enableButtons());
                TreeAnimator.getInstance().animateQueue();
            } catch (NumberFormatException e) {
                showInputErrorAlert("Please enter a valid integer key.");
            }
        });
    }

    private Dialog<String[]> createKeyValueDialog(String title, String header, boolean includeDataField) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType confirmButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        TextField keyField = new TextField();
        keyField.setPromptText("Key");
        VBox vbox = new VBox(keyField);

        TextField dataField = null;
        if (includeDataField) {
            dataField = new TextField();
            dataField.setPromptText("Data");
            vbox.getChildren().add(dataField);

            // Listener para actualizar el valor por defecto dinámicamente
            TextField finalDataField = dataField;
            keyField.textProperty().addListener((_, _, newVal) -> {
                finalDataField.setText("Data " + newVal);
            });
            finalDataField.setOnMouseClicked(_ -> finalDataField.setText(""));
        }


        dialog.getDialogPane().setContent(vbox);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                String[] result = new String[includeDataField ? 2 : 1];
                result[0] = keyField.getText();
                if (includeDataField) {
                    result[1] = ((TextField) vbox.getChildren().get(1)).getText();
                }
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void showInputErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, "Invalid Input", message);
    }

    
    public boolean insert(Integer key, String data) {
        BPlusTraversalResult<Boolean, Integer, String> result = this.tree.insert(key, data);
        treeView.animateTraversal(result.getVisitedKeys());
        return result.getResult();
    }

    public Key<Integer> remove(Integer key) {
        BPlusTraversalResult<Key<Integer>, Integer, String> result = this.tree.remove(key);
        treeView.animateTraversal(result.getVisitedKeys());
        return result.getResult();
    }

    public BPlusLeafNode<Integer, String> search(Integer key) {
        BPlusTraversalResult<BPlusLeafNode<Integer, String>, Integer, String>  result =  this.tree.search(key);
        treeView.animateTraversal(result.getVisitedKeys());
        return result.getResult();
    }

    private void disableButtons() {
        this.insertBtn.setDisable(true);
        this.removeBtn.setDisable(true);
        this.searchBtn.setDisable(true);
        this.homeBtn.setDisable(true);
    }

    private void enableButtons() {
        this.insertBtn.setDisable(false);
        this.removeBtn.setDisable(false);
        this.searchBtn.setDisable(false);
        this.homeBtn.setDisable(false);
        this.treeView.updateKeyViews();
    }

}