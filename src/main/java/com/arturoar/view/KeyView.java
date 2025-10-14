package com.arturoar.view;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

public class KeyView extends Group {
    
    private Rectangle nodeShape;
    private Text keyLabel;
    private Label dataLabel;
    private Integer key;
    private double currentXOrigin;
    private double currentYOrigin;
    private double newXOrigin;
    private double newYOrigin;
    private Double width;
    private Double height;
    private boolean isNew;
    private final Double paddingX = 8.0;
    private final Double paddingY = 4.0;
    private Color strokeColor = Color.BLACK;
    private Color fillColor = Color.WHITE;
    private NodeView node;
    private NodeView newNode;

    public KeyView(Integer key) {
        this.key = key;
        this.keyLabel = new Text(this.key.toString());
        this.width = this.keyLabel.getLayoutBounds().getWidth() + 2 * paddingX;
        this.height = this.keyLabel.getLayoutBounds().getHeight() + 2 * paddingY;
        this.isNew = true;
        setupNode();
    }
    public KeyView(Integer key, String nodeData ) {
        this(key);
        dataLabel = new Label(nodeData);
        dataLabel.setVisible(false);
        //dataBox = new VBox(dataLabel);
        
        // Configuración básica del Label
        dataLabel.setMinHeight(this.width);
        dataLabel.setMaxHeight(35.0);
        dataLabel.setMinWidth(this.height);
        dataLabel.setMaxWidth(200.0);
        dataLabel.setAlignment(Pos.BASELINE_LEFT);
        dataLabel.setWrapText(true);
        dataLabel.setStyle("-fx-padding: 0 5 0 5;"); // Espaciado interno

        // LIMPIA cualquier transformación previa
        dataLabel.getTransforms().clear();

        // Aplicar rotación -90° desde la esquina superior izquierda
        Rotate rotate = new Rotate(90, 0, 0);
        dataLabel.getTransforms().add(rotate);

        // Posicionamiento ABSOLUTO en el contenedor padre
        dataLabel.setLayoutX(0);  // Alineado al borde izquierdo del contenedor
        dataLabel.setLayoutY(dataLabel.getHeight() + this.height + 10);  // 5 unidades por debajo del contenedor
        dataLabel.setTranslateX(this.width ); // 5 unidades a la derecha del contenedor

         dataLabel.getStyleClass().add("rotated-label-modern");


        // Contenedor
        /*
        dataBox.setAlignment(Pos.CENTER);
        dataBox.setStyle("-fx-background-color: #0d2ba4f8; -fx-padding: 0;");
        dataBox.setMaxWidth(this.width);

        dataBox.setTranslateY(this.height + 5);*/

        getChildren().add(dataLabel);
    }


    private void setupNode(){
        
        nodeShape = new Rectangle(this.width, this.height);
        nodeShape.setStrokeWidth(1);
        nodeShape.setStroke(this.strokeColor);
        nodeShape.setFill(this.fillColor);
        
        keyLabel.setX(paddingX);
        keyLabel.setY(this.height - (2.0 * paddingY));
        
        getChildren().addAll(this.nodeShape, this.keyLabel);

        ChangeListener<String> strListener = (_, _, newValue) -> {
            this.setKey(Integer.valueOf(newValue));
        };

        this.keyLabel.textProperty().addListener(strListener);
    }

    public NodeView getNode() {
        return this.node;
    }

    public void setNode(NodeView node) {
        this.node = node;
    }

    public void setNewNode(NodeView newNode) {
        this.newNode = newNode;
    }   

    public void updateNode() {
        this.node = this.newNode;
    }
   
    public double getNewXOrigin(){
        return this.newXOrigin;
    }

    public double getNewYOrigin(){
        return this.newYOrigin;
    }   

    public void setNewOriginX(Double newOriginX) {
        this.newXOrigin = newOriginX;
    }

    public void setNewOriginY(Double newOriginY) {
        this.newYOrigin = newOriginY;
    }

    public double getCurrentXOrigin() {
        return this.currentXOrigin;
    }

    public double getCurrentYOrigin() {
        return this.currentYOrigin;
    }

    public void setCurrentXOrigin(Double currentXOrigin) {
        this.currentXOrigin = currentXOrigin;
    }

    public void setCurrentYOrigin(Double currentYOrigin) {
        this.currentYOrigin = currentYOrigin;
    } 

    public double getDeltaX(){
        double deltaX  = this.newXOrigin - this.currentXOrigin;
        return deltaX;
    }

    public double getDeltaY(){
        double deltaY  = this.newYOrigin - this.currentYOrigin;
        return deltaY;
    }
    
    public boolean isNew() {
        return isNew;
    }


    public void setWidth(Double width) {
        this.width = width;
        this.nodeShape.setWidth(this.width);
    }

    public Double getHeight() {
        return this.height;
    }
    public Double getWidth() {
        return this.width;
    }
    public Integer getKey() {
        return this.key;
    }
    public Text getKeyLabel() {
        return this.keyLabel;
    }
    public void setKey(Integer key) {
        this.key = key;
        this.width = this.keyLabel.getLayoutBounds().getWidth() + 2 * paddingX;
        this.height = this.keyLabel.getLayoutBounds().getHeight() + 2 * paddingY;
        this.nodeShape.setWidth(this.width);
        this.nodeShape.setHeight(this.height);
    }
    public void setFillColor(int r, int g, int b) {
        this.fillColor = Color.rgb(r, g, b);
        this.nodeShape.setFill(this.fillColor);
    }
    public void setStrokeColor(int r, int g, int b) {
        this.strokeColor = Color.rgb(r, g, b);
        this.nodeShape.setStroke(this.strokeColor);
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public Rectangle getKeyShape() {
        return this.nodeShape;
    }

}
