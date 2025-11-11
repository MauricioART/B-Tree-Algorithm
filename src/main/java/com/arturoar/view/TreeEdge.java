package com.arturoar.view;

import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;

public class TreeEdge extends Edge {

    private CubicCurve body;

    public TreeEdge() {
        super();
        edgeType = EdgeType.TREE_EDGE;
        body = new CubicCurve();
        body.setStrokeWidth(2);
        body.setStroke(Color.BLACK);
        body.setFill(null);
        initialize();
    }

    @Override
    protected void initialize() {
        body.startXProperty().bind(originX);
        body.startYProperty().bind(originY);
        body.endXProperty().bind(endX);
        body.endYProperty().bind(endY);

        getChildren().addAll(body, head);
        updateControlPoints();
        updateArrowHead(edgeType.getAngle());

        // Update control points + arrowhead when coords change
        ChangeListener<Number> listener = (_, _, _) -> {
            updateControlPoints();
            updateArrowHead(edgeType.getAngle());
        };

        originX.addListener(listener);
        originY.addListener(listener);
        endX.addListener(listener);
        endY.addListener(listener);

     
    }

    
    private void updateControlPoints() {  
        // Puntos de control para garantizar tangentes verticales
        double midY = (originY.get() + endY.get()) / 2;

        ((CubicCurve)body).setControlX1(originX.get());
        ((CubicCurve)body).setControlY1(midY);
        ((CubicCurve)body).setControlX2(endX.get());
        ((CubicCurve)body).setControlY2(midY);
    }

    public CubicCurve getBody() {
        return (CubicCurve) this.body;
    }
    
}
