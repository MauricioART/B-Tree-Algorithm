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
    private double newOriginX;
    private double newOriginY;
    private Double width;
    private Double height;
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

   
    public double getNewOriginX(){
        return this.newOriginX;
    }

    public double getNewOriginY(){
        return this.newOriginY;
    }   

    public void setNewOriginX(Double newOriginX) {
        this.newOriginX = newOriginX;
    }

    public void setNewOriginY(Double newOriginY) {
        this.newOriginY = newOriginY;
    }   

    public double getDeltaX(){
        double deltaX  = this.newOriginX - this.translateXProperty().get();
        return deltaX;
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

}
