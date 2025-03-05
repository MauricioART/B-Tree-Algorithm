package com.arturoar.controller;

import java.util.Optional;


import com.arturoar.model.BPlusTree;
import com.arturoar.view.BPlusNodeView;

//import com.arturoar.view.BPlusPageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.fxml.FXML;

public class BPlusTreeController {
    private BPlusTree tree;
    @FXML private Pane canvas; // Panel donde se dibuja el árbol
    @FXML private Button insertBtn;
    @FXML private Button removeBtn;
    @FXML private Button searchBtn;
    @FXML private Button homeBtn;

    private static final double SCALE_DELTA = 1.1;  // Factor de zoom
    private Scale scaleTransform;
    private Translate translateTransform;
    private double zoomFactor = 1.0;
    private double lastMouseX, lastMouseY;
    private Group treeGroup;

    public BPlusTreeController() {
        this.tree = new BPlusTree(3);
        this.treeGroup = new Group();
    }

    @FXML
    public void initialize() {

        
        scaleTransform = new Scale(1, 1, 0, 0); // Zoom centrado en (0,0)
        translateTransform = new Translate();

        this.treeGroup.getTransforms().addAll(scaleTransform, translateTransform);

        this.canvas.getChildren().add(treeGroup);

        this.canvas.setOnScroll(this::handleZoom);
        this.canvas.setOnMousePressed(this::handleMousePressed);
        this.canvas.setOnMouseDragged(this::handleMouseDragged);

        this.homeBtn.setOnAction(e->goHome());

        this.removeBtn.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Remove Node");
            dialog.setHeaderText("Insert the key:");
            ButtonType insertButtonType = new ButtonType("Insert", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);


            TextField textField = new TextField();
            textField.setPromptText("Key");
            dialog.getDialogPane().setContent(textField);

            // Obtener el resultado cuando el usuario presiona "Aceptar"
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == insertButtonType) {
                    return textField.getText();
                }
                return null;
            });

            // Mostrar el diálogo y obtener la entrada del usuario
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(key -> {
                if (!this.remove(Integer.parseInt(key))){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Removal Error");
                    alert.setHeaderText("The structure does not contain " + key + " key.");
                    // Mostrar la alerta
                    alert.showAndWait();
                }else{
                    updateView();
                }
            });
        });
        this.insertBtn.setOnAction(e -> {
            System.out.println("Insertar");
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("New Node");
            dialog.setHeaderText("Insert the new key:");
            ButtonType insertButtonType = new ButtonType("Insert", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);


            TextField keyField = new TextField();
            keyField.setPromptText("New key");
            TextField dataField = new TextField();
            dataField.setPromptText("Data");
            VBox vbox = new VBox();
            vbox.getChildren().addAll(keyField, dataField);
            dialog.getDialogPane().setContent(vbox);

            // Obtener el resultado cuando el usuario presiona "Aceptar"
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == insertButtonType) {
                    return keyField.getText() + "," + dataField.getText();
                }
                return null;
            });

            // Mostrar el diálogo y obtener la entrada del usuario
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(input -> {
                String[] values = input.split(",");
                if (!this.insert(Integer.parseInt(values[0]), values[1])){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Insertion Error");
                    alert.setHeaderText("Key already exists");
                    // Mostrar la alerta
                    alert.showAndWait();
                }else{
                    updateView();
                }

            });
        });

        this.searchBtn.setOnAction(e -> {
            this.search(10);
            updateView();
        });
    }

    private void goHome() {
        scaleTransform.setX(1);
        scaleTransform.setY(1);
    }
    private void handleZoom(ScrollEvent event) {
        scaleTransform.setPivotX(event.getX()); // Zoom centrado en el cursor
        scaleTransform.setPivotY(event.getY());
        double zoomFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
        System.err.println("Zoom factor: " + this.zoomFactor);
                
        this.zoomFactor *= zoomFactor;
        if (this.zoomFactor > 0.3 && this.zoomFactor < 2) {
            scaleTransform.setX(scaleTransform.getX() * zoomFactor);
            scaleTransform.setY(scaleTransform.getY() * zoomFactor);
        }
        if (this.zoomFactor < 0.3) {
            this.zoomFactor = 0.3;
        }
        if (this.zoomFactor > 2) {
            this.zoomFactor = 2;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        ((Pane) event.getSource()).setCursor(Cursor.CLOSED_HAND);
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        System.err.println("Mouse pressed at: " + lastMouseX + ", " + lastMouseY);
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - lastMouseX;
        double deltaY = event.getSceneY() - lastMouseY;

        translateTransform.setX(translateTransform.getX() + deltaX );
        translateTransform.setY(translateTransform.getY() + deltaY );

        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        
        ((Pane) event.getSource()).setCursor(Cursor.OPEN_HAND);
    }

    
    public boolean insert(int key,String data) {
       return this.tree.insertNode(key, data);
    }
    public boolean remove(int key) {
        return this.tree.removeNode(key);
    }
    public void search(int key) {
        this.tree.searchKey(key);
    }

    public void animate() {
        this.tree.mostrarArbol();
    }


    private void updateView() {
        this.tree.mostrarArbol();
    }

}
