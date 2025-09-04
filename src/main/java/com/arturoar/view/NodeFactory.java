package com.arturoar.view;

public class NodeFactory {
    
    public static NodeView createNode(boolean isLeaf, Double x, Double y){
        if (isLeaf){
            return new LeafNodeView(x, y);
        } else {
            return new InnerNodeView(x, y);
        }
    }
}
