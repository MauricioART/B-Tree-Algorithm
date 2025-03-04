package com.arturoar.model;

/**
 * This class represents the information unit to store in the structure.
 * @author Aguilera Roa Mauricio Arturo
 */
public class BPlusNode {
    
    private String data1;
    private int data2;

    /**
     * Constructs an instance of this class, initializing data1 and data2 with the given values.
     * @param data1 String to initialize data1
     * @param data2 Integer to initialize data2
     */
    public BPlusNode(String data1, int data2) {
        this.data1 = data1;
        this.data2 = data2;
    }

    /**
     * Constructs an instance of the BPlusNode class, initializing data1.
     * @param data1 String to initialize data1
     */
    public BPlusNode(String data1) {
        this.data1 = data1;
        this.data2 = 0;
    }
    
    public String getData1() {
        return data1;
    }
    
    public void setData1(String data1) {
        this.data1 = data1;
    }
    
    public int getData2() {
        return data2;
    }
    
    public void setData2(int data2) {
        this.data2 = data2;
    }

    @Override
    public String toString() {
        return "◆◆◆◆◆◆◆◆◆ Node Data ◆◆◆◆◆◆◆◆◆\nName: " + this.data1 
                + "\nAge: " + data2;
    }
}