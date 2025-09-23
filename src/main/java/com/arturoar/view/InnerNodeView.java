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


    public int insert(KeyView newKeyView, boolean hasSplit){
        int position = super.insert(newKeyView);

        if (!hasSplit){

            Arrow prevArrow = this.edges.get(position);
            prevArrow.originXProperty().unbind();
            prevArrow.originYProperty().unbind();
            prevArrow.originXProperty().bind(newKeyView.translateXProperty());
            prevArrow.originYProperty().bind(newKeyView.translateYProperty().add(newKeyView.getHeight()));

            Arrow nextArrow = this.edges.get(position + 1);
            if (position < super.keys.size() - 1) {
                KeyView nextKeyView = super.keys.get(position + 1);
                nextArrow.originXProperty().bind(nextKeyView.translateXProperty());
                nextArrow.originYProperty().bind(nextKeyView.translateYProperty().add(nextKeyView.getHeight()));
            }else{
                KeyView lastKey = super.keys.getLast();
                nextArrow.originXProperty().bind(lastKey.translateXProperty().add(lastKey.getWidth()));
                nextArrow.originYProperty().bind(lastKey.translateYProperty().add(lastKey.getHeight()));
            }
       
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

}
