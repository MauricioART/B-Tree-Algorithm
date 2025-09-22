package com.arturoar.view;

import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class KeyView extends Group {
    
    private Rectangle nodeShape;
    private Text text;
    private Tooltip nodeData;
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

    public KeyView(Integer key) {
        this.key = key;
        this.text = new Text(this.key.toString());
        this.nodeData = null;
        this.width = this.text.getLayoutBounds().getWidth() + 2 * paddingX;
        this.height = this.text.getLayoutBounds().getHeight() + 2 * paddingY;
        this.isNew = true;
        setupNode();
    }
    public KeyView(Integer key, String nodeData ) {
        this(key);
        this.nodeData = new Tooltip(nodeData);
        Tooltip.install(this.nodeShape, this.nodeData);
    }


    private void setupNode(){
        this.nodeShape = new Rectangle(this.width, this.height);
        this.nodeShape.setStrokeWidth(1);
        this.nodeShape.setStroke(this.strokeColor);
        this.nodeShape.setFill(this.fillColor);
        this.text.setX(paddingX);
        this.text.setY(this.height - (2.0 * paddingY));
        this.getChildren().addAll(this.nodeShape, this.text);

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
    public void setKey(Integer key) {
        this.key = key;
        this.text.setText(this.key.toString());
        this.width = this.text.getLayoutBounds().getWidth() + 2 * paddingX;
        this.height = this.text.getLayoutBounds().getHeight() + 2 * paddingY;
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
}
