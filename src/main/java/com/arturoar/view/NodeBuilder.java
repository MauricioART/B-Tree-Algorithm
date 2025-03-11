package com.arturoar.view;

public interface NodeBuilder {
    NodeBuilder setOrigin(Double originX, Double originY);
    NodeBuilder setKey(Integer key);
    NodeBuilder setNodeData(String nodeData);
    BPlusNodeView build();
}