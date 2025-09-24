package com.arturoar.controller;

import com.arturoar.model.*;
import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.TreeAnimator;
import com.arturoar.view.TreeView;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class BPlusTreeController {

    private static final double SCALE_DELTA = 1.1;
   // private static final double DEFAULT_X_SPACING = 20.0;
   // private static final double DEFAULT_Y_SPACING = 80.0;
    private static final int DEFAULT_BRANCHING_FACTOR = 3;

    @FXML private Pane canvas;
    @FXML private Button insertBtn;
    @FXML private Button removeBtn;
    @FXML private Button searchBtn;
    @FXML private Button homeBtn;
    //@FXML private AnchorPane anchorPane;

    private final Scale scaleTransform = new Scale(1, 1);
    private final Translate translateTransform = new Translate();

    
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
    
    private TreeView treeView;
    

    public BPlusTreeController() {
        this.tree = new BPlusTree<>(this.branchingFactor);
        this.treeView = new TreeView(this.tree.getRoot());
        this.tree.addObserver(this.treeView);
    }

    @FXML
    public void initialize() {
        setupTransforms();
        setupCanvas();
        setupButtonActions();
    }

    private void setupTransforms() {
        this.treeView.getTransforms().addAll(scaleTransform, translateTransform);
    }

    private void setupCanvas() {
        this.canvas.getChildren().add(treeView);
        this.canvas.setOnScroll(this::handleZoom);
        this.canvas.setOnMousePressed(this::handleMousePressed);
        this.canvas.setOnMouseDragged(this::handleMouseDragged);
        this.treeView.canvasHeightProperty().bind(this.canvas.heightProperty());
        this.treeView.canvasWidthProperty().bind(this.canvas.widthProperty());

        this.canvas.setOnMouseClicked(_event -> {
            // Handle mouse click events on the canvas
            System.out.println("Canvas clicked at: " + _event.getX() + ", " + _event.getY());
        });
    }

    private void setupButtonActions() {
        this.homeBtn.setOnAction  (_ -> resetView());
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
                if (remove(key).getResult() == null) {
                    showAlert(Alert.AlertType.ERROR, "Removal Error", 
                            "The structure does not contain key " + key);
                }
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
                BPlusTraversalResult<Key<Integer>, Integer, String> result = search(key);
                if ( result.getResult() == null) {
                    showAlert(Alert.AlertType.ERROR, "Search Error", 
                            "The structure does not contain key " + key);
                }else{
                    // TODOw: Implement search animation
                    System.out.println("◆◆◆◆◆ Búsqueda exitosa ◆◆◆◆◆");
                }
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
        
        if (includeDataField) {
            TextField dataField = new TextField();
            dataField.setPromptText("Data");
            vbox.getChildren().add(dataField);
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

    private void handleZoom(ScrollEvent event) {
        double scaleFactor = event.getDeltaY() > 0 ? SCALE_DELTA : 1 / SCALE_DELTA;
        scaleTransform.setX(scaleTransform.getX() * scaleFactor);
        scaleTransform.setY(scaleTransform.getY() * scaleFactor);
        //zoomFactor *= scaleFactor;
    }

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - lastMouseX;
        double deltaY = event.getSceneY() - lastMouseY;
        
        translateTransform.setX(translateTransform.getX() + deltaX);
        translateTransform.setY(translateTransform.getY() + deltaY);
        
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void resetView() {
        scaleTransform.setX(1);
        scaleTransform.setY(1);
        translateTransform.setX(0);
        translateTransform.setY(0);
        //zoomFactor = 1.0;
    }   

    public boolean insert(Integer key, String data) {
        BPlusTraversalResult<Boolean, Integer, String> result = this.tree.insert(key, data);
        return result.getResult();
    }

    public BPlusTraversalResult<Key<Integer>, Integer, String> remove(Integer key) {
        return this.tree.remove(key);
    }

    public BPlusTraversalResult<Key<Integer>, Integer, String> search(Integer key) {
        return this.tree.search(key);
    }

    
}