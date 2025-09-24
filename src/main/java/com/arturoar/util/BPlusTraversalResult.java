package com.arturoar.util;

import java.util.ArrayList;

import com.arturoar.model.BPlusNode;

public class BPlusTraversalResult<T, K extends Comparable<K>, V> {
    private T result;
    private ArrayList<BPlusNode<K,V>> visitedNodes;

    public BPlusTraversalResult() {
        this.result = null;
        this.visitedNodes = new ArrayList<>();
    }
    public BPlusTraversalResult(ArrayList<BPlusNode<K,V>> visitedNodes) {
        this.result = null;
        this.visitedNodes = visitedNodes;
    }
    public BPlusTraversalResult(ArrayList<BPlusNode<K,V>> visitedNodes, T result){
        this.result = result;
        this.visitedNodes = visitedNodes;
    }

    public BPlusTraversalResult(T result){
        this.result = result;
        this.visitedNodes = new ArrayList<>();
    }

    public void addVisitedKey(BPlusNode<K,V> node) {
        this.visitedNodes.add(node);
    }

    public void setVisitedKeys(ArrayList<BPlusNode<K,V>> nodes){
        this.visitedNodes = nodes;
    }
    public ArrayList<BPlusNode<K,V>> getVisitedNodes() {
        return visitedNodes;
    }

    public void setResult(T result){
        this.result = result;
    }
    public T getResult() {
        return this.result;
    }
}
