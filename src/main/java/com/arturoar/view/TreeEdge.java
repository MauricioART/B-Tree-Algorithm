package com.arturoar.view;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.shape.CubicCurve;

public class TreeEdge extends Edge {

    private CubicCurve body;

    public TreeEdge() {
        super();
        edgeType = EdgeType.TREE_EDGE;
        body = new CubicCurve();
        body.setStrokeWidth(1.4);
        body.strokeProperty().bind(colorProperty);
        body.setFill(null);
        initialize();
    }

    @Override
    protected void initialize() {
        body.startXProperty().bind(originX);
        body.startYProperty().bind(originY);
        body.endXProperty().bind(endX);
        body.endYProperty().bind(endY);

        DoubleBinding midpoint = originY.add(endY).divide(2);
        body.controlX1Property().bind(originX);
        body.controlX2Property().bind(endX);
        body.controlY1Property().bind(midpoint);
        body.controlY2Property().bind(midpoint);

        configureArrowHead(edgeType.getAngle());
        getChildren().addAll(body, head);
    }

    public CubicCurve getBody() {
        return (CubicCurve) this.body;
    }
    
}
