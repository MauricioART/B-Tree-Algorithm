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
