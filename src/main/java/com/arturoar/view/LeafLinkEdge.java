package com.arturoar.view;

import javafx.scene.shape.Line;

public class LeafLinkEdge extends Edge {

    private Line body;

    public LeafLinkEdge() {
        super();
        edgeType = EdgeType.LEAF_LINK_EDGE;
        this.body = new Line();
        body.setStrokeWidth(1.3);
        body.getStrokeDashArray().setAll(4.0, 3.0);
        body.strokeProperty().bind(colorProperty);
        body.fillProperty().bind(colorProperty);
        initialize();
    }


    @Override
    protected void initialize() {   
        body.startXProperty().bind(originX);
        body.startYProperty().bind(originY);
        body.endXProperty().bind(endX);
        body.endYProperty().bind(endY);

        configureArrowHead(edgeType.getAngle());
        getChildren().addAll(body, head);
    }

    public Line getBody() {
        return body;
    }

    public void setBody(Line body) {
        this.body = body;
    }
}

