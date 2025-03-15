package com.arturoar.model;

import java.util.ArrayList;

public class BPlusLeafNode<K extends Comparable<K>, V> extends BPlusNode<K,V>{
    
    private ArrayList<V> data;
    private BPlusLeafNode<K, V> nextLeafNode;


    public BPlusLeafNode(boolean isLeaf, int B, int level,BPlusInternalNode<K,V> parent){
        super(isLeaf, B, level, parent);
        this.data = new ArrayList<>();
        this.nextLeafNode = null;
    }

    public ArrayList<V> getData(){
        return this.data;
    }

    public V getData(int index){
        return this.data.get(index);
    }

    public BPlusLeafNode<K, V> getNextLeafNode(){
        return this.nextLeafNode;
    }

    public void setData(ArrayList<V> data){
        this.data = data;
    }

    public void setData(int index, V data){
        this.data.set(index, data);
    }

    public void setNextLeafNode(BPlusLeafNode<K,V> nextLeaf){
        this.nextLeafNode = nextLeaf;
    }
}
