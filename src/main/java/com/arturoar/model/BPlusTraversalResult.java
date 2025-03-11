package com.arturoar.model;

import java.util.ArrayList;

public class BPlusTraversalResult<T> {
    private T result;
    private ArrayList<BPlusNode> visitedNodes;

    public BPlusTraversalResult() {
        this.result = null;
        this.visitedNodes = new ArrayList<>();
    }
    public BPlusTraversalResult(ArrayList<BPlusNode> visiteNodes) {
        this.result = null;
        this.visitedNodes = visiteNodes;
    }
    public BPlusTraversalResult(ArrayList<BPlusNode> visiteNodes, T result){
        this.result = result;
        this.visitedNodes = visiteNodes;
    }

    public BPlusTraversalResult(T result){
        this.result = result;
        this.visitedNodes = new ArrayList<>();
    }

    public void addVisitedNode(BPlusNode node) {
        this.visitedNodes.add(node);
    }

    public void setVisitedNodes(ArrayList<BPlusNode> nodes){
        this.visitedNodes = nodes;
    }
    public ArrayList<BPlusNode> getVisitedNodes() {
        return visitedNodes;
    }

    public void setResult(T result){
        this.result = result;
    }
    public T getResult() {
        return this.result;
    }
}
