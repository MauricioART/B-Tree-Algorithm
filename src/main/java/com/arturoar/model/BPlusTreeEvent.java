package com.arturoar.model;

public class BPlusTreeEvent<K extends Comparable<K>,V> {
    public enum EventType { NODE_CREATED, NODE_DELETED, KEY_BORROWED,  KEY_INSERTED, KEY_REMOVED }

    private final EventType type;
    private final BPlusNode<K,V> affectedNode;
    private final K affectedKey;

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> affectedNode, K affectedKey) {
        this.type = type;
        this.affectedNode = affectedNode;
        this.affectedKey = affectedKey;
    }


    public EventType getType() { return type; }
    public BPlusNode<K,V> getAffectedNode() { return affectedNode; }
    public K getAffectedKey() { return affectedKey; }
}
