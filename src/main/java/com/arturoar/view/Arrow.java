package com.arturoar.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;

public class Arrow extends Group {

    private final CubicCurve curve;
    private final Polygon arrowHead;

    private final DoubleProperty originX = new SimpleDoubleProperty();
    private final DoubleProperty originY = new SimpleDoubleProperty();
    private final DoubleProperty endX = new SimpleDoubleProperty();
    private final DoubleProperty endY = new SimpleDoubleProperty();
    private final ObjectProperty<Color> arrowColor = new SimpleObjectProperty<>(Color.BLACK);

    public Arrow() {
        this(0, 0, 0, 0);
    }

    public Arrow(double originX, double originY, double endX, double endY) {
        this.originX.set(originX);
        this.originY.set(originY);
        this.endX.set(endX);
        this.endY.set(endY);

        this.curve = new CubicCurve();
        this.curve.setStrokeWidth(1);
        this.curve.setStroke(Color.BLACK);
        this.curve.setFill(null);

        this.arrowHead = new Polygon();
        this.arrowHead.fillProperty().bind(this.arrowColor);

        getChildren().addAll(curve, arrowHead);

        initialize();
    }

    private void initialize() {
        // Bind start and end points of the curve
        this.curve.startXProperty().bind(originX);
        this.curve.startYProperty().bind(originY);
        this.curve.endXProperty().bind(endX);
        this.curve.endYProperty().bind(endY);

        // Update control points + arrowhead when coords change
        ChangeListener<Number> listener = (_, _, _) -> {
            updateControlPoints();
            updateArrowHead();
        };

        originX.addListener(listener);
        originY.addListener(listener);
        endX.addListener(listener);
        endY.addListener(listener);

        updateControlPoints();
        updateArrowHead();
    }

    private void updateControlPoints() {
        // Puntos de control para garantizar tangentes verticales
        double midY = (originY.get() + endY.get()) / 2;

        curve.setControlX1(originX.get());
        curve.setControlY1(midY);

        curve.setControlX2(endX.get());
        curve.setControlY2(midY);
    }

    private void updateArrowHead() {
        // Flecha siempre hacia abajo (vertical)
        double angle = Math.PI / 2; // 90 grados

        double arrowSize = 8.0;
        double ex = endX.get();
        double ey = endY.get();

        double x1 = ex - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = ey - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = ex - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = ey - arrowSize * Math.sin(angle + Math.PI / 6);

        arrowHead.getPoints().setAll(
                ex, ey,
                x1, y1,
                x2, y2
        );
    }

    // Getters/Setters/Properties
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

    public final Color getArrowColor() { return arrowColor.get(); }
    public final void setArrowColor(Color value) { arrowColor.set(value); }
    public ObjectProperty<Color> arrowColorProperty() { return arrowColor; }
}
