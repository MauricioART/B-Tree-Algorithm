package com.arturoar.view;

import javafx.scene.Group;
import java.util.ArrayList;

public class BPlusPageView extends Group {
    
    private Double originX;
    private Double originY;
    private Double width;
    private Double heigth;
    private boolean isLeaf;
    private ArrayList<BPlusNodeView> nodes;
    private ArrayList<Arrow> edges;

    public BPlusPageView(boolean isLeaf) {
        this.originX = null;
        this.originY = null;
        this.width = 0.0;
        this.isLeaf = isLeaf;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.edges.add(new Arrow());
    }
    

    public BPlusPageView(Double x, Double y, boolean isLeaf) {
        this.originX = x;
        this.originY = y;
        this.width = 0.0;
        this.isLeaf = isLeaf;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.edges.add(new Arrow());
    }
    

    public void insertNode(BPlusNodeView newNodeView){
        this.width += newNodeView.getWidth();
        this.nodes.add(newNodeView);
        this.edges.add(new Arrow());
    }

    public void insertNodeOld(int key, String nodeData) {
        int position = 0;
        for (BPlusNodeView node : this.nodes) {
            if (node.getKey() < key) {
                position++;
            }else { break; }
        }
        Double originX = this.nodes.stream()
                                        .filter(n -> n.getKey() < key)
                                        .map(n -> n.getWidth())
                                        .reduce(0.0, (a, b) -> a + b);

        BPlusNodeBuilder builder = new BPlusNodeBuilder();
        builder.setOrigin(originX, this.originY).setKey(key);

        if (this.isLeaf){
            builder.setNodeData(nodeData);
        }
        BPlusNodeView node = builder.build();

        this.nodes.add(position, node);
        for (int i = position + 1; i < this.nodes.size(); i++) {
            this.nodes.get(i).translateOriginX(node.getWidth());
        }
        this.width += node.getWidth();
        this.getChildren().add(node);
    }

    public void removeNode(Integer key) {
        BPlusNodeView node = this.nodes.stream()
                                    .filter(n -> n.getKey().equals(key))
                                    .findFirst()
                                    .orElse(null);
        if (node != null) {
            int nodeIndex = this.nodes.indexOf(node);
            Double nodeWidth = this.nodes.get(nodeIndex).getWidth();
            this.width -= nodeWidth;
            this.nodes.remove(node);
            for (int i = nodeIndex; i < this.nodes.size(); i++) {
                this.nodes.get(i).translateOriginX(-nodeWidth);
            }
            this.getChildren().remove(node);
        }
    }

    public void updateViewParameters(){

    }
    public Arrow getEdge(int index){
        return this.edges.get(index);
    }

    public Double getOriginX() {
        return this.originX;
    }
    public Double getOriginY() {
        return this.originY;
    }
    public Double getWidth() {
        return this.width;
    }

    public Double getHeight(){
        return this.nodes.get(0).getHeight();
    }
    public boolean getIsLeaf(){
        return this.isLeaf;
    }
    
    public void setIsLeaf(boolean isLeaf){
         this.isLeaf = isLeaf;
    }
    public ArrayList<Arrow> getEdges(){
        return this.edges;
    }

    public void setOrigin(Double originX, Double originY) {
        this.originX = originX;
        this.originY = originY;
        int i = 0;
        this.heigth = this.nodes.get(0).getHeight(); 
        for (BPlusNodeView node : this.nodes) {
            node.setNewOriginX(originX);
            node.setNewOriginY(originY);
            this.edges.get(i++).setOrigin(originX, originY + this.heigth);
            originX += node.getWidth();
        }
        this.edges.get(i).setOrigin(originX, originY + this.heigth);   
    }

    public Double getPageMiddleX(){
        return this.originX + this.width / 2;
    }
}