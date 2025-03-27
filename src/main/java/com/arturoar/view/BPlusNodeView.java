package com.arturoar.view;

import javafx.scene.Group;
import java.util.ArrayList;

public class BPlusNodeView extends Group {
    
    private Double originX;
    private Double originY;
    private Double width;
    private Double heigth;
    private boolean isLeaf;
    private ArrayList<BPlusKeyView> keys;
    private ArrayList<Arrow> edges;
    private Arrow nextLeaf;

    public BPlusNodeView(boolean isLeaf) {
        this.originX = null;
        this.originY = null;
        this.width = 0.0;
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.edges.add(new Arrow());
        this.nextLeaf = new Arrow();
    }
    

    public BPlusNodeView(Double x, Double y, boolean isLeaf) {
        this.originX = x;
        this.originY = y;
        this.width = 0.0;
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.edges.add(new Arrow());
    }
    

    public void appendKey(BPlusKeyView newNodeView){
        this.width += newNodeView.getWidth();
        this.keys.add(newNodeView);
        this.edges.add(new Arrow());
    }

    public void insert(BPlusKeyView newKeyView){
        int position = 0;
        for (BPlusKeyView key : this.keys) {
            if (key.getKey() < newKeyView.getKey()) {
                position++;
            }else { break; }
        }
        Double originX = this.keys.stream()
                                        .filter(n -> n.getKey() < newKeyView.getKey())
                                        .map(n -> n.getWidth())
                                        .reduce(0.0, (a, b) -> a + b);

        newKeyView.setNewOriginX(originX);
        newKeyView.setNewOriginY(this.originY);
        this.keys.add(position, newKeyView);
        for (int index = position + 1; index < this.keys.size(); index++) {
            this.keys.get(index).translateOriginX(newKeyView.getWidth());
        }
        this.width += newKeyView.getWidth();
        this.getChildren().add(newKeyView);
    }


    public void removeNode(Integer key) {
        BPlusKeyView node = this.keys.stream()
                                    .filter(n -> n.getKey().equals(key))
                                    .findFirst()
                                    .orElse(null);
        if (node != null) {
            int nodeIndex = this.keys.indexOf(node);
            Double nodeWidth = this.keys.get(nodeIndex).getWidth();
            this.width -= nodeWidth;
            this.keys.remove(node);
            for (int i = nodeIndex; i < this.keys.size(); i++) {
                this.keys.get(i).translateOriginX(-nodeWidth);
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
        return this.keys.get(0).getHeight();
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
        this.heigth = this.keys.get(0).getHeight(); 
        int keyIndex = 0;
        for (BPlusKeyView keyView : this.keys) {
            keyView.setNewOriginX(originX);
            keyView.setNewOriginY(originY);
            this.edges.get(keyIndex++).setOrigin(originX, originY + this.heigth);
            originX += keyView.getWidth();
        }
        this.edges.get(keyIndex).setOrigin(originX, originY + this.heigth);   
    }

    public Double getPageMiddleX(){
        return this.originX + this.width / 2;
    }
}