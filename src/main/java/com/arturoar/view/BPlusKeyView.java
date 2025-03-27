package com.arturoar.view;

import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class BPlusKeyView extends Group {
    
    private Rectangle nodeShape;
    private Text text;
    private Tooltip nodeData;
    private Integer key;
    private Double originX;
    private Double originY;
    private Double newOriginX;
    private Double newOriginY;
    private Double width;
    private Double height;
    private final Double paddingX = 8.0;
    private final Double paddingY = 4.0;
    private Color strokeColor = Color.BLACK;
    private Color fillColor = Color.WHITE;

    public BPlusKeyView(Integer key) {
        this.originX = null;
        this.originY = null;
        this.key = key;
        this.text = new Text(this.key.toString());
        this.nodeData = null;
        this.width = this.text.getLayoutBounds().getWidth() + 2 * paddingX;
        this.height = this.text.getLayoutBounds().getHeight() + 2 * paddingY;
        setupNode();
    }
    public BPlusKeyView(Integer key, String nodeData ) {
        this.originX = null;
        this.originY = null;
        this.key = key;
        this.text = new Text(this.key.toString());
        this.nodeData = new Tooltip(nodeData);
        Tooltip.install(this.nodeShape, this.nodeData);
        this.width = this.text.getLayoutBounds().getWidth() + 2 * paddingX;
        this.height = this.text.getLayoutBounds().getHeight() + 2 * paddingY;
        setupNode();
    }


    private void setupNode(){
        this.nodeShape = new Rectangle(this.width, this.height);
        this.nodeShape.setStrokeWidth(1);
        this.nodeShape.setStroke(this.strokeColor);
        this.nodeShape.setFill(this.fillColor);
        this.getChildren().addAll(this.nodeShape, this.text);
    }

    public void setOrigin(Double originX, Double originY) {
        this.originX = originX;
        this.originY = originY;
        this.nodeShape.setX(originX);
        this.nodeShape.setY(originY);
        this.text.setX(originX + paddingX);
        this.text.setY(originY + this.height - 2* paddingY);
    }


    public void updatePosition(){
        this.originX = this.newOriginX;
        this.originY = this.newOriginY;
        setOrigin(this.originX, this.originY);
    }

    public void translateOriginX(Double distance) {
        this.originX += distance;
        this.nodeShape.setX(this.originX);
        this.text.setX(this.originX + paddingX);
    }
    public void setNewOriginX(Double newOriginX){
        this.newOriginX = newOriginX;
    }
    public void setNewOriginY(Double newOriginY){
        this.newOriginY = newOriginY;
    }
    
    public Double getOriginX() {
        return this.originX;
    }
    public Double getOriginY() {
        return this.originY;
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
    public Double getNewOriginX(){
        return this.newOriginX;
    }
    public Double getNewOriginY(){
        return this.newOriginY;
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
