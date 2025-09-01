package com.arturoar.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Arrow extends Group{
    
    private Line line;
    private Polygon arrowHead;
    private final DoubleProperty originX = new SimpleDoubleProperty();
    private final DoubleProperty originY = new SimpleDoubleProperty();
    private final DoubleProperty endX = new SimpleDoubleProperty();
    private final DoubleProperty endY = new SimpleDoubleProperty();
    private final ObjectProperty<Color> arrowColor = new SimpleObjectProperty<>(Color.BLACK);

    public Arrow() {
        this.originX.set(0.0);
        this.originY.set(0.0);
        this.endX.set(0.0);
        this.endY.set(0.0);
        this.line = new Line();
        this.line.setStrokeWidth(1);
        

        // Line angle
        double angle = Math.atan2(this.endY.doubleValue() - this.originY.doubleValue(), this.endX.doubleValue() - this.originX.doubleValue());

        // Longitud del triángulo
        double arrowSize = 10.0; 

        // Puntos del triángulo (cabeza de la flecha)
        double x1 = endX.get() - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = endY.get() - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = endX.get() - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = endY.get() - arrowSize * Math.sin(angle + Math.PI / 6);

        
        this.arrowHead = new Polygon();
        this.arrowHead.getPoints().addAll(endX.get(), endY.get(), x1, y1, x2, y2);
        this.arrowHead.fillProperty().bind(this.arrowColor);

        Group  arrow = new Group();
        arrow.getChildren().add(line);
        arrow.getChildren().add(this.arrowHead);
        this.getChildren().add(arrow);

        initialize();
    }

    public Arrow(Double originX, Double originY, Double endX, Double endY) {
        this.originX.set(originX);
        this.originY.set(originY);
        this.endX.set(endX);
        this.endY.set(endY);
        this.line = new Line();
        this.line.setStrokeWidth(1);

        // Line angle
        double angle = Math.atan2(this.endY.doubleValue() - this.originY.doubleValue(), this.endX.doubleValue() - this.originX.doubleValue());

        // Longitud del triángulo
        double arrowSize = 10.0; 

        // Puntos del triángulo (cabeza de la flecha)
        double x1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);

        
        this.arrowHead = new Polygon();
        this.arrowHead.getPoints().addAll(endX, endY, x1, y1, x2, y2);
        this.arrowHead.fillProperty().bind(this.arrowColor);


        Group  arrow = new Group();
        arrow.getChildren().add(line);
        arrow.getChildren().add(this.arrowHead);
        this.getChildren().add(arrow);

        initialize();
    }

    
    private void initialize() {
        this.line.startXProperty().bind(this.originX);
        this.line.startYProperty().bind(this.originY);
        this.line.endXProperty().bind(this.endX);
        this.line.endYProperty().bind(this.endY);

        ChangeListener<Number> listener = (_, _, _) -> updateArrowHead();

        this.originX.addListener(listener);
        this.originY.addListener(listener);
        this.endX.addListener(listener);
        this.endY.addListener(listener);
    }

     private void updateArrowHead(){
        
        // Line angle
        double angle = Math.atan2(this.endY.get() - this.originY.get(), this.endX.get() - this.originX.get());

        // Longitud del triángulo
        double arrowSize = 10.0; 

        // Puntos del triángulo (cabeza de la flecha)
        double x1 = endX.doubleValue() - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = endY.doubleValue() - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = endX.doubleValue() - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = endY.doubleValue() - arrowSize * Math.sin(angle + Math.PI / 6);

    
        this.arrowHead.getPoints().setAll(endX.get(), endY.get(), x1, y1, x2, y2);

    }


    public final double getOriginX() {
        return originX.get();
    }

    public final void setOriginX(double value) {
        originX.set(value);
    }

    public DoubleProperty originXProperty() {
        return originX;
    }

    public final double getOriginY() {
        return originY.get();
    }

    public final void setOriginY(double value) {
        originY.set(value);
    }

    public DoubleProperty originYProperty() {
        return originY;
    }

    public final double getEndX() {
        return endX.get();
    }

    public final void setEndX(double value) {
        endX.set(value);
    }

    public DoubleProperty endXProperty() {
        return endX;
    }

    public final double getEndY() {
        return endY.get();
    }

    public final void setEndY(double value) {
        endY.set(value);
    }

    public DoubleProperty endYProperty() {
        return endY;
    }

    public final Color getArrowColor() {
        return arrowColor.get();
    }

    public final void setArrowColor(Color value) {
        arrowColor.set(value);
    }

    public ObjectProperty<Color> arrowColorProperty() {
        return arrowColor;
    }

   
         

}
