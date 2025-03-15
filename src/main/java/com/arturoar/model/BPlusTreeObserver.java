package com.arturoar.model;

public interface BPlusTreeObserver {
    void onTreeChanged(BPlusTreeEvent event);
}
