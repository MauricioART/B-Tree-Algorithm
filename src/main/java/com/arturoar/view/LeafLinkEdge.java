package com.arturoar.view;

import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class LeafLinkEdge extends Edge {

    private Line body;

    public LeafLinkEdge() {
        super();
        edgeType = EdgeType.LEAF_LINK_EDGE;
        this.body = new Line();
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

        updateArrowHead(edgeType.getAngle());
        getChildren().addAll(body, head);

        ChangeListener<Number> listener = (_,_, _) -> {
            updateArrowHead(edgeType.getAngle());
        };

        endX.addListener(listener);
        endY.addListener(listener);
    }

   
    public Line getBody() {
        return body;
    }

    public void setBody(Line body) {
        this.body = body;
    }
}

