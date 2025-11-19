package com.arturoar.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public abstract class Edge extends Group {

    protected final Paint LIGHT_COLOR = Color.web("#000");
    protected final Paint DARK_COLOR = Color.web("#FFF");

    protected Polygon head;
    protected EdgeType edgeType;

    protected final DoubleProperty originX = new SimpleDoubleProperty();
    protected final DoubleProperty originY = new SimpleDoubleProperty();
    protected final DoubleProperty endX = new SimpleDoubleProperty();
    protected final DoubleProperty endY = new SimpleDoubleProperty();
    protected final ObjectProperty<Paint> colorProperty = new SimpleObjectProperty<>(LIGHT_COLOR);
    protected BooleanProperty darkModeProperty = new SimpleBooleanProperty(false);

    public Edge() {
        this(0, 0, 0, 0);
    }

    public Edge(double originX, double originY, double endX, double endY) {
        this.originX.set(originX);
        this.originY.set(originY);
        this.endX.set(endX);
        this.endY.set(endY);

        this.head = new Polygon();
        this.head.fillProperty().bind(colorProperty);

        darkModeProperty.addListener((_,_, newVal)->{
            if (newVal){
                colorProperty.set(DARK_COLOR);
            }else{
                colorProperty.set(LIGHT_COLOR);
            }
        });


    }

    protected abstract void initialize();

    protected void updateArrowHead(double angle) {

        double arrowSize = 8.0;
        double ex = endX.get();
        double ey = endY.get();

        double x1 = ex - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = ey - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = ex - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = ey - arrowSize * Math.sin(angle + Math.PI / 6);

        head.getPoints().setAll(
                ex, ey,
                x1, y1,
                x2, y2
        );
    }


    // Getters/Setters/Properties

    public Polygon getHead() {
        return this.head;
    }

    public final double getOriginX() { return originX.get(); }
    public final void setOriginX(double value) { originX.set(value); }
    public DoubleProperty originXProperty() { return originX; }

    public final double getOriginY() { return originY.get(); }
    public final void setOriginY(double value) { originY.set(value); }
    public DoubleProperty originYProperty() { return originY; }

    public final double getEndX() { return endX.get(); }
    public final void setEndX(double value) { endX.set(value); }
    public DoubleProperty endXProperty() { return endX; }

    public final double getEndY() { return endY.get(); }
    public final void setEndY(double value) { endY.set(value); }
    public DoubleProperty endYProperty() { return endY; }

    public final Paint getColorProperty() { return colorProperty.get(); }
    public final void setArrowColor(Color value) { colorProperty.set(value); }
    public ObjectProperty<Paint> colorProperty() { return colorProperty; }

    public BooleanProperty darkModeProperty() { return darkModeProperty; }
    
    public enum EdgeType {
        TREE_EDGE(Math.PI / 2),
        LEAF_LINK_EDGE(0);

        private final double angle;
    
        EdgeType(double angle) {
            this.angle = angle;
        }
    
        public double getAngle() {
            return angle;
        }
    }
}