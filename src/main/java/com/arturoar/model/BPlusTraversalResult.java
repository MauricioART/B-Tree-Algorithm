package com.arturoar.model;

import java.util.ArrayList;

public class BPlusTraversalResult<T, K extends Comparable<K>> {
    private T result;
    private ArrayList<K> visitedNodes;

    public BPlusTraversalResult() {
        this.result = null;
        this.visitedNodes = new ArrayList<>();
    }
    public BPlusTraversalResult(ArrayList<K> visiteNodes) {
        this.result = null;
        this.visitedNodes = visiteNodes;
    }
    public BPlusTraversalResult(ArrayList<K> visiteNodes, T result){
        this.result = result;
        this.visitedNodes = visiteNodes;
    }

    public BPlusTraversalResult(T result){
        this.result = result;
        this.visitedNodes = new ArrayList<>();
    }

    public void addVisitedNode(K node) {
        this.visitedNodes.add(node);
    }

    public void setVisitedNodes(ArrayList<K> nodes){
        this.visitedNodes = nodes;
    }
    public ArrayList<K> getVisitedNodes() {
        return visitedNodes;
    }

    public void setResult(T result){
        this.result = result;
    }
    public T getResult() {
        return this.result;
    }
}
