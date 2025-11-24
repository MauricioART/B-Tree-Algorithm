package com.arturoar.exceptions;

import java.io.EOFException;

public class BPlusTreeEmptyException extends EOFException  {
    
    public BPlusTreeEmptyException() {
        super("B+ Tree is empty.");
    }
    
}
