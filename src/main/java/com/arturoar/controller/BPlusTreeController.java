package com.arturoar.controller;

import com.arturoar.model.*;
import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.TreeAnimator;
import com.arturoar.util.TreeViewTransformer;
import com.arturoar.view.TreeView;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class BPlusTreeController {

   // private static final double DEFAULT_X_SPACING = 20.0;
   // private static final double DEFAULT_Y_SPACING = 80.0;
    private static final int DEFAULT_BRANCHING_FACTOR = 4;

    @FXML private Pane canvas;
    @FXML private Button insertBtn;
    @FXML private Button removeBtn;
    @FXML private Button searchBtn;
    @FXML private Button homeBtn;
    //@FXML private AnchorPane anchorPane;
    
    //private double xSpacing = DEFAULT_X_SPACING;
    //private double ySpacing = DEFAULT_Y_SPACING;
    private int branchingFactor = DEFAULT_BRANCHING_FACTOR;
    //private double zoomFactor = 1.0;
    private double lastMouseX;
    private double lastMouseY;
    //private Double centerX;
    //private Double centerY;

    // BPlusTree model instance
    private final BPlusTree<Integer, String> tree;
    private TreeViewTransformer transformer;
    
    private TreeView treeView;
    

    public BPlusTreeController() {
        this.tree = new BPlusTree<>(this.branchingFactor);
        this.treeView = new TreeView(this.tree.getRoot());
        this.tree.addObserver(this.treeView);

        
    }

    @FXML
    public void initialize() {
        setupCanvas();
        this.canvas.setOnMouseClicked(event->{
            this.lastMouseX = event.getX();
            this.lastMouseY = event.getY();
            System.out.println("Coords: (" + this.lastMouseX + ", " + this.lastMouseY + ")");
        });
        this.transformer = new TreeViewTransformer(canvas, treeView);
        setupButtonActions();
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
        this.homeBtn.setOnAction  (_ -> transformer.resetView());
        this.insertBtn.setOnAction(_ -> handleInsert());
        this.removeBtn.setOnAction(_ -> handleRemove());
        this.searchBtn.setOnAction(_ -> handleSearch());
    }

    
    
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
                TreeAnimator.addListenerToLastTransition(() -> enableButtons());

                TreeAnimator.animateQueue();
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
                TreeAnimator.addListenerToLastTransition(() -> enableButtons());
                this.tree.showTree();
                TreeAnimator.animateQueue();
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
                TreeAnimator.addListenerToLastTransition(() -> enableButtons());
                TreeAnimator.animateQueue();
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