package com.arturoar.view;


import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Arrow extends Group{
    
    private Line line;
    private Polygon arrowHead;
    private Double originX;
    private Double originY;
    private Double endX;
    private Double endY;
    private Color arrowColor = Color.BLACK;

    public Arrow(){
        this.originX = null;
        this.originY = null;
        this.endX = null;
        this.endY = null;
        this.line = new Line();
        this.line.setStrokeWidth(1);
        this.line.setStroke(this.arrowColor);
    }
    public Arrow(Double originX, Double originY, Double endX, Double endY) {
        this.originX = originX;
        this.originY = originY;
        this.endX = endX;
        this.endY = endY;
        this.line = new Line(originX, originY, endX, endY);
        this.line.setStrokeWidth(1);
        this.line.setStroke(this.arrowColor);
        

        // Line angle
        double angle = Math.atan2(this.endY - this.originY, this.endX - this.originX);

        // Longitud del triángulo
        double arrowSize = 10.0; 

        // Puntos del triángulo (cabeza de la flecha)
        double x1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);

        
        this.arrowHead = new Polygon();
        this.arrowHead.getPoints().addAll(endX, endY, x1, y1, x2, y2);
        this.arrowHead.setFill(this.arrowColor);

        Group  arrow = new Group();
        arrow.getChildren().add(line);
        arrow.getChildren().add(this.arrowHead);
        this.getChildren().add(arrow);
    }

    public void setArrowColor(Color color){
        this.arrowColor = color;
        this.line.setStroke(this.arrowColor);
        this.arrowHead.setFill(this.arrowColor);
    }

    public void setOrigin(Double originX, Double originY){
        this.originX = originX;
        this.originY = originY;
    }
    
    public void setEnd(Double endX, Double endY){
        this.endX = endX;
        this.endY = endY;
    }

    public void setupArrow(){
        this.line = new Line(originX, originY, endX, endY);
        this.line.setStrokeWidth(1);
        this.line.setStroke(this.arrowColor);
        

        // Line angle
        double angle = Math.atan2(this.endY - this.originY, this.endX - this.originX);

        // Longitud del triángulo
        double arrowSize = 10.0; 

        // Puntos del triángulo (cabeza de la flecha)
        double x1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);

        
        this.arrowHead = new Polygon();
        this.arrowHead.getPoints().addAll(endX, endY, x1, y1, x2, y2);
        this.arrowHead.setFill(this.arrowColor);

        Group  arrow = new Group();
        arrow.getChildren().add(line);
        arrow.getChildren().add(this.arrowHead);
        this.getChildren().add(arrow);
    }

}
