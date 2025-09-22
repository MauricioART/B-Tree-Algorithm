package com.arturoar.view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Group;

public class InnerNodeView extends NodeView {

    public List<Arrow> edges = new ArrayList<>();

    private KeyView dummyKey;

    public InnerNodeView(Double x, Double y) {
        super(x, y);
        addDummyKey();
    }

    public InnerNodeView() {
        super();
        addDummyKey();
    }

    public void addDummyKey() {
        dummyKey = new KeyView(Integer.MAX_VALUE);
        dummyKey.setWidth(0.0);
        dummyKey.setOpacity(0.0); // Opcional: invisible
        super.insert(dummyKey);
        // If you need to add dummyKey to the scene graph, cast getParent() to a suitable Pane subclass:
                if (super.getParent() instanceof Group) {
                    ((Group) super.getParent()).getChildren().add(dummyKey); // No mostrar en la vista
                }
                // Otherwise, remove this line if not needed, as getChildren() is not visible for Parent.
    }

    public KeyView getDummyKey() {
        return dummyKey;
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
                nextArrow.originXProperty().bind(this.translateXProperty().add(this.widthProperty()));
                nextArrow.originYProperty().bind(this.translateYProperty().add(this.getHeight()));
            }
       
        }
        return position;
    }

    public void bindToEndNode(Arrow edge){
        edge.originXProperty().unbind();
        edge.originYProperty().unbind();
        edge.originXProperty().bind(dummyKey.translateXProperty());
        edge.originYProperty().bind(dummyKey.translateYProperty().add(dummyKey.getHeight()));
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
