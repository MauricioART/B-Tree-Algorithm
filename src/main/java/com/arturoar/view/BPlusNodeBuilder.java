package com.arturoar.view;

public class BPlusNodeBuilder implements NodeBuilder {
    
    private Double originX;
    private Double originY;
    private Integer key;
    private String nodeData;

    @Override
    public NodeBuilder setOrigin(Double originX, Double originY) {
        this.originX = originX;
        this.originY = originY;
        return this;
    }

    @Override
    public NodeBuilder setNodeData(String nodeData) {
        this.nodeData = nodeData;
        return this;
    }

    @Override
    public NodeBuilder setKey(Integer key) {
        this.key = key;
        return this;
    }


    @Override
    public BPlusNodeView build() {
        return this.nodeData == null ? new BPlusNodeView(key) : new BPlusNodeView(key, nodeData);
    }
}
