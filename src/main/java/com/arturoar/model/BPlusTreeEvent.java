package com.arturoar.model;

public class BPlusTreeEvent<K extends Comparable<K>,V> {
    public enum EventType { NODE_CREATED, NODE_DELETED, KEY_BORROWED,  KEY_INSERTED, KEY_REMOVED }

    private final EventType type;
    private final BPlusNode<K,V> affectedNode;
    private final BPlusNode<K,V> affectedNode2;
    private final Key<K> affectedKey;

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> affectedNode, Key<K> affectedKey) {
        this.type = type;
        this.affectedNode = affectedNode;
        this.affectedKey = affectedKey;
        this.affectedNode2 = null;
    }

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> lenderNode, BPlusNode<K,V> borrowerNode, Key<K> affectedKey) {
        this.type = type;
        this.affectedNode = lenderNode;
        this.affectedKey = affectedKey;
        this.affectedNode2 = borrowerNode;
    }
    


    public EventType getType() { return type; }
    public BPlusNode<K,V> getAffectedNode() { return affectedNode; }
    public Key<K> getAffectedKey() { return affectedKey; }
    public BPlusNode<K,V> getAffectedNode2() { return affectedNode2; }
}
