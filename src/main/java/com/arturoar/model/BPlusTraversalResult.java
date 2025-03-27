package com.arturoar.model;

import java.util.ArrayList;

public class BPlusTraversalResult<T, K extends Comparable<K>> {
    private T result;
    private ArrayList<Key<K>> visitedKeys;

    public BPlusTraversalResult() {
        this.result = null;
        this.visitedKeys = new ArrayList<>();
    }
    public BPlusTraversalResult(ArrayList<Key<K>> visiteNodes) {
        this.result = null;
        this.visitedKeys = visiteNodes;
    }
    public BPlusTraversalResult(ArrayList<Key<K>> visiteNodes, T result){
        this.result = result;
        this.visitedKeys = visiteNodes;
    }

    public BPlusTraversalResult(T result){
        this.result = result;
        this.visitedKeys = new ArrayList<>();
    }

    public void addVisitedKey(Key<K> node) {
        this.visitedKeys.add(node);
    }

    public void setVisitedKeys(ArrayList<Key<K>> nodes){
        this.visitedKeys = nodes;
    }
    public ArrayList<Key<K>> getVisitedKeys() {
        return visitedKeys;
    }

    public void setResult(T result){
        this.result = result;
    }
    public T getResult() {
        return this.result;
    }
}
