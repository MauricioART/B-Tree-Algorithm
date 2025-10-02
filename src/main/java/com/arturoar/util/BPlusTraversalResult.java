package com.arturoar.util;

import java.util.ArrayList;

import com.arturoar.model.Key;

public class BPlusTraversalResult<T, K extends Comparable<K>, V> {
    private T result;
    private ArrayList<Key<K>> visitedKeys;

    public BPlusTraversalResult() {
        this.result = null;
        this.visitedKeys = new ArrayList<>();
    }
    public BPlusTraversalResult(ArrayList<Key<K>> visitedKeys) {
        this.result = null;
        this.visitedKeys = visitedKeys;
    }
    public BPlusTraversalResult(ArrayList<Key<K>> visitedKeys, T result){
        this.result = result;
        this.visitedKeys = visitedKeys;
    }

    public BPlusTraversalResult(T result){
        this.result = result;
        this.visitedKeys = new ArrayList<>();
    }

    public void addVisitedKey(Key<K> key) {
        this.visitedKeys.add(key);
    }

    public void setVisitedKeys(ArrayList<Key<K>> keys){
        this.visitedKeys = keys;
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
