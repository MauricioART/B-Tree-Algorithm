package com.arturoar.view;

public class LeafNodeView extends NodeView {

    public Arrow nextLeaf;

    public LeafNodeView(Double x, Double y) {
        super(x, y);
        initialize();
    }

    public LeafNodeView() {
        super();
        initialize();
    }

    private void initialize() {
        this.nextLeaf = new Arrow();
        this.nextLeaf.originXProperty().bind(this.translateXProperty().add(this.widthProperty()));
        this.nextLeaf.originYProperty().bind(this.translateYProperty());
        this.nextLeaf.endXProperty().bind(this.nextLeaf.originXProperty());
        //this.nextLeaf.opacityProperty().set(0.0);
        //this.getChildren().add(this.nextLeaf);
    }

    public Arrow getNextLeaf() {
        return nextLeaf;
    }
}
