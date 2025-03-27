package com.arturoar.model;

import java.util.ArrayList;
import java.util.stream.Collectors;

abstract public class BPlusNode<K extends Comparable<K>, V> {

    private int B;
    private ArrayList<Key<K>> keys;
    private BPlusInternalNode<K, V> parent;
    private boolean isLeaf;
    private int level;

    BPlusNode(boolean isLeaf, int B, int level, BPlusInternalNode<K, V> parent) {
        this.keys = new ArrayList<>();
        this.isLeaf = isLeaf;
        this.parent = parent;
        this.B = B;
    }

    public BPlusInternalNode<K, V> getParent() {
        return this.parent;
    }

    public boolean isLeaf() {
        return this.isLeaf;
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

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setKey(int index, Key<K> key) {
        this.keys.set(index, key);
    }

    public void setKeys(ArrayList<Key<K>> keys) {
        this.keys = keys;
    }

    public void setParent(BPlusInternalNode<K, V> parent) {
        this.parent = parent;
    }

    public boolean isOverFlow() {
        return this.keys.size() > (2 * B) - 1;
    }

    public boolean isUnderFlow() {
        return this.keys.size() < B - 1;
    }

    public int size() {
        return this.keys.size();
    }

    public int getChildrenIndex() {
        if (this.parent == null) {
            return -1;
        } else {
            BPlusInternalNode<K, V> parent = this.parent;
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
        this.level += 1;
    }

    public void decreaseLevel(){
        this.level -= 1;
    }
}
