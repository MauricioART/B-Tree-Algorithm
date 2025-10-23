package com.arturoar.model;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

abstract public class BPlusNode<K extends Comparable<K>, V> {

    protected int m;
    protected ArrayList<Key<K>> keys;
    protected BPlusInnerNode<K, V> parent;
    protected boolean isLeaf;
    protected IntegerProperty level;

    BPlusNode(boolean isLeaf, int m, int level, BPlusInnerNode<K, V> parent) {
        this.keys = new ArrayList<>();
        this.isLeaf = isLeaf;
        this.parent = parent;
        this.m = m;
        this.level = new SimpleIntegerProperty(level);
    }

    public BPlusInnerNode<K, V> getParent() {
        return this.parent;
    }

    public boolean isLeaf() {
        return this.isLeaf;
    }

    public boolean isEmpty(){
        return keys.size() == 0;
    }

    public ArrayList<K> getKeyValues() {
        return this.keys.stream().map(Key::getKey).collect(Collectors.toCollection(ArrayList::new));
    }


    public ArrayList<Key<K>> getKeys() {
        return this.keys;
    }

    public Key<K> deleteKey(K key){
        for (Key<K> storedKey : this.keys) {
            if (storedKey.getKey().equals(key)) {
                this.keys.remove(storedKey);
                return storedKey;
            }
        }
        return null;
    }
    public Key<K> deleteKey(int index){
        return this.keys.remove(index);
    }

    public void addKey(Key<K> key){
        this.keys.add(key);
    }

    public void addKey(int index, Key<K> key){
        this.keys.add(index, key);
    }

    public K getKey(int index) {
        return this.keys.get(index).getKey();
    }

    public Key<K> getKeyObject(int index) {
        return this.keys.get(index);
    }
    public int getLevel() {
        return this.level.get();
    }

    public IntegerProperty levelProperty(){
        return level;
    }

    public void setLevel(int level) {
        this.level.set(level);;
    }

    public void setKey(int index, Key<K> key) {
        this.keys.set(index, key);
    }

    public void setKeys(ArrayList<Key<K>> keys) {
        this.keys = keys;
    }

    public void setParent(BPlusInnerNode<K, V> parent) {
        this.parent = parent;
    }

    public boolean isOverFlow() {
        return this.keys.size() > m - 1;
    }

    public boolean isUnderFlow() {
        return this.keys.size() < Math.ceil(m / 2) - 1;
    }

    public boolean wouldBeUnderFlow() {
    return (this.size() - 1) < Math.ceil(m / 2) - 1;
}

    public int size() {
        return this.keys.size();
    }

    public int getChildrenIndex() {
        if (this.parent == null) {
            return -1;
        } else {
            BPlusInnerNode<K, V> parent = this.parent;
            for (int childrenIndex = 0; childrenIndex < parent.getChildren().size(); childrenIndex++) {
                if (parent.getChild(childrenIndex) == this)
                    return childrenIndex;
            }
            return -1;
        }
    }

    public void showKeys(){
        for( int i = 0 ; i < this.keys.size() ; i++ )
            System.out.print( this.keys.get(i).getKey() + " " );
    }
    
    public void increaseLevel() {
        level.set(level.get() + 1);
    }

    public void decreaseLevel(){
        level.set(level.get() - 1);
    }
}
