package com.arturoar.model;

import java.util.ArrayList;

public class BPlusInternalNode<K extends Comparable<K>, V> extends BPlusNode<K, V> {

    private ArrayList<BPlusNode<K, V>> children;

    public BPlusInternalNode(boolean isLeaf, int B, int level, BPlusInternalNode<K, V> parent) {
        super(isLeaf, B, level, parent);
        this.children = new ArrayList<>();
    }

    public ArrayList<BPlusNode<K, V>> getChildren() {
        return this.children;
    }

    public BPlusNode<K, V> getChild(int index) {
        return this.children.get(index);
    }

    public void setChildren(ArrayList<BPlusNode<K, V>> children) {
        this.children = children;
    }

    public void setChild(int index, BPlusNode<K, V> child) {
        this.children.set(index, child);
    }
}
