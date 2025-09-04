package com.arturoar.view;

import java.util.ArrayList;
import java.util.List;

public class InnerNodeView extends NodeView {


    public List<Arrow> edges = new ArrayList<>();

    public InnerNodeView(Double x, Double y) {
        super(x, y);
    }

    public InnerNodeView() {
        super();
    }


    @Override
    public void appendKey(KeyView newNodeView) {
        super.appendKey(newNodeView);
        this.newEdge(newNodeView, this.keys.size() - 1);
    }

    @Override
    public int insert(KeyView newKeyView) {
        

        int position = super.insert(newKeyView);

        Arrow prevArrow = this.edges.get(position);
        prevArrow.originXProperty().unbind();
        prevArrow.originYProperty().unbind();
        prevArrow.originXProperty().bind(this.translateXProperty().add(newKeyView.translateXProperty()));
        prevArrow.originYProperty().bind(this.translateYProperty().add(newKeyView.translateYProperty()).add(newKeyView.getHeight()));


        Arrow nextArrow = this.edges.get(position + 1);

        if (position < super.keys.size() - 1) {
            KeyView nextKeyView = super.keys.get(position + 1);
            nextArrow.originXProperty().bind(nextKeyView.translateXProperty());
            nextArrow.originYProperty().bind(nextKeyView.translateYProperty().add(nextKeyView.getHeight()));
        }else{
            nextArrow.originXProperty().bind(this.translateXProperty().add(this.widthProperty()));
            nextArrow.originYProperty().bind(this.translateYProperty().add(this.getHeight()));
        }
        return position;
    }

    
    public Arrow getEdge(int index){
        return this.edges.get(index);
    }
    
    public List<Arrow> getEdges(){
        return this.edges;
    }
    public void addEdge(int index, Arrow edge){
        this.edges.add(index, edge);
    }

    private void newEdge(KeyView keyView, int position) {
        Arrow edge = new Arrow();
        edge.originXProperty().bind(keyView.translateXProperty());
        edge.originYProperty().bind(keyView.translateYProperty().add(keyView.getHeight()));
        this.edges.add(position, edge);
        this.getChildren().add(edge);
    }


}
