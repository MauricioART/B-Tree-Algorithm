package com.arturoar.util;

public interface BPlusTreeObserver<K extends Comparable<K>,V> {
    void onTreeChanged(BPlusTreeEvent<K,V> event);
}
