package com.arturoar.view;

import java.util.ArrayList;
import java.util.List;


public class InnerNodeView extends NodeView {

    public List<Edge> edges = new ArrayList<>();


    public InnerNodeView(Double x, Double y) {
        super(x, y);
    }

    public InnerNodeView() {
        super();
    }


    public Edge getEdge(int index){
        return this.edges.get(index);
    }
    
    public List<Edge> getEdges(){
        return this.edges;
    }

    public void addEdge(int index, Edge edge){
        this.edges.add(index, edge);
    }

}
