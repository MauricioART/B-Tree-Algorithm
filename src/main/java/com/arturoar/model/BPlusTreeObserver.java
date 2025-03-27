package com.arturoar.model;

public interface BPlusTreeObserver<K extends Comparable<K>,V> {
    void onTreeChanged(BPlusTreeEvent<K,V> event);
}
