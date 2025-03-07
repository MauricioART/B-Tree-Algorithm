package com.arturoar.model;

public class BPlusNode {

    private int key;

    public BPlusNode(int key){
        this.key = key;
    }

    public int getKey(){
        return this.key;
    }
    public void setKey(int key){
        this.key = key;
    }

}
