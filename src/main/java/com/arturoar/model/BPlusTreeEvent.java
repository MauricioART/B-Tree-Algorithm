package com.arturoar.model;

public class BPlusTreeEvent<K extends Comparable<K>,V> {
    public enum EventType { NODE_CREATED, NODE_DELETED, KEY_BORROWED,  KEY_INSERTED, KEY_REMOVED,  CHILDNODE_BORROWED, CHILDNODE_DELETED, CHILDNODE_CREATED}

    private final EventType type;
    private final BPlusNode<K,V> affectedNode;
    private final BPlusNode<K,V> lendingNode;
    private final BPlusNode<K,V> borrowingNode;
    private final BPlusNode<K,V> childrenNode;
    private final Key<K> affectedKey;

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> affectedNode, Key<K> affectedKey) {
        this.type = type;
        this.affectedNode = affectedNode;
        this.affectedKey = affectedKey;
        this.lendingNode = null;
        this.borrowingNode = null;
        this.childrenNode = null;
    }

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> lendingNode, BPlusNode<K,V> borrowingNode, Key<K> affectedKey) {
        this.type = type;
        this.affectedNode = null;
        this.affectedKey = affectedKey;
        this.lendingNode = lendingNode;
        this.borrowingNode = borrowingNode;
        this.childrenNode = null;
    }
    
    public BPlusTreeEvent(EventType type, BPlusNode<K,V> lendingNode, BPlusNode<K,V> borrowingNode, BPlusNode<K,V> childrenNode) {
        this.type = type;
        this.affectedNode = null;
        this.affectedKey = null;
        this.lendingNode = lendingNode;
        this.borrowingNode = borrowingNode;
        this.childrenNode = childrenNode;
    }

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> node, BPlusNode<K,V> childrenNode) {
        this.type = type;
        this.affectedNode = node;
        this.affectedKey = null;
        this.lendingNode = null;
        this.borrowingNode = null;
        this.childrenNode = childrenNode;
    }
    

    public BPlusTreeEvent(EventType type, BPlusNode<K,V> affectedNode) {
        this.type = type;
        this.affectedNode = affectedNode;
        this.affectedKey = null;
        this.lendingNode = null;
        this.borrowingNode = null;
        this.childrenNode = null;
    }
    


    public EventType getType() { return type; }
    public BPlusNode<K,V> getAffectedNode() { return affectedNode; }
    public Key<K> getAffectedKey() { return affectedKey; }
    public BPlusNode<K,V> getLendingNode() { return lendingNode; }
    public BPlusNode<K,V> getBorrowingNode() { return borrowingNode; }
    public BPlusNode<K,V> getChildrenNode() { return childrenNode; }
}
