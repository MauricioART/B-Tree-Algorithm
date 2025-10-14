package com.arturoar.view;

public class LeafNodeView extends NodeView {

    public Edge nextLeaf;

    public LeafNodeView(Double x, Double y) {
        super(x, y);
        initialize();
    }

    public LeafNodeView() {
        super();
        initialize();
    }

    private void initialize() {
        this.nextLeaf = new LeafLinkEdge();
        this.nextLeaf.originXProperty().bind(this.translateXProperty().add(this.animatedWidth));
        this.nextLeaf.originYProperty().bind(this.translateYProperty().add(this.height.divide(2)));
        this.nextLeaf.opacityProperty().set(0.0);
    }

    public Edge getNextLeaf() {
        return nextLeaf;
    }

    public void setNextLeaf(Edge nextLeaf) {
        nextLeaf.originXProperty().unbind();
        nextLeaf.originYProperty().unbind();
        nextLeaf.originXProperty().bind(this.translateXProperty().add(this.animatedWidth));
        nextLeaf.originYProperty().bind(this.translateYProperty().add(this.height.divide(2)));
        this.nextLeaf = nextLeaf;
        
    }
}
