package com.arturoar.model;

/**
 * This class represents the information unit to store in the structure.
 * @author Aguilera Roa Mauricio Arturo
 */
public class BPlusLeafNode extends BPlusNode {
    
    private String data;

    /**
     * Constructs an instance of this class, initializing data and data2 with the given values.
     * @param data String to initialize data
     * @param key Integer to initialize data2
     */
    public BPlusLeafNode(int key, String data) {
        super(key);
        this.data = data;
    }

    /**
     * Constructs an instance of the BPlusNode class, initializing data.
     * @param data String to initialize data
     */
    public BPlusLeafNode(int key) {
        super(key);
        this.data = "";
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public int getKey() {
        return super.getKey();
    }
    

    @Override
    public String toString() {
        return "◆◆◆◆◆◆◆◆◆ Node Data ◆◆◆◆◆◆◆◆◆\nName: " + this.data 
                + "\nAge: " + super.getKey();
    }
}