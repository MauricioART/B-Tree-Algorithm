package com.arturoar.util;

import java.util.List;

import com.arturoar.model.*;

public abstract class BPlusTreeEvent<K extends Comparable<K>, V> {

    public enum EventType { 
        NODE_SPLIT, KEY_BORROWED, KEY_INSERTED, 
        KEY_REMOVED, NODE_BORROWED, NODE_DELETED, 
        NODE_CREATED, NEW_ROOT , KEY_CHANGED
    }

    private final EventType type;

    protected BPlusTreeEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    // ==================== SUBTYPES ====================

    public static class NodeSplit<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> node;
        private final BPlusNode<K, V> newNode;
        private final Key<K> middleKey;

        public NodeSplit(BPlusNode<K, V> node, BPlusNode<K, V> newNode, Key<K> middleKey) {
            super(EventType.NODE_SPLIT);
            this.node = node;
            this.newNode = newNode;
            this.middleKey = middleKey;
        }

        public BPlusNode<K, V> getNode() {
            return node;
        }
        public BPlusNode<K, V> getNewNode() {
            return newNode;
        }
        public Key<K> getMiddleKey() {
            return middleKey;
        }
    }

    
    public static class KeyBorrowed<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> lendingNode;
        private final BPlusNode<K, V> borrowingNode;
        private final List<Key<K>> borrowedKeys;

        public KeyBorrowed(BPlusNode<K, V> lendingNode, BPlusNode<K, V> borrowingNode, List<Key<K>> borrowedKeys) {
            super(EventType.KEY_BORROWED);
            this.lendingNode = lendingNode;
            this.borrowingNode = borrowingNode;
            this.borrowedKeys = borrowedKeys;
        }

        public BPlusNode<K, V> getLendingNode() { return lendingNode; }
        public BPlusNode<K, V> getBorrowingNode() { return borrowingNode; }
        public List<Key<K>> getBorrowedKeys() { return borrowedKeys; }
    }

    public static class KeyInserted<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> node;
        private final Key<K> key;

        public KeyInserted(BPlusNode<K, V> node, Key<K> key) {
            super(EventType.KEY_INSERTED);
            this.node = node;
            this.key = key;
        }

        public BPlusNode<K, V> getNode() { return node; }
        public Key<K> getKey() { return key; }
    }

    public static class KeyRemoved<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> node;
        private final Key<K> key;

        public KeyRemoved(BPlusNode<K, V> node, Key<K> key) {
            super(EventType.KEY_REMOVED);
            this.node = node;
            this.key = key;
        }

        public BPlusNode<K, V> getNode() { return node; }
        public Key<K> getKey() { return key; }
    }


    public static class NodeBorrowed<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> lendingNode;
        private final BPlusNode<K, V> borrowingNode;
        private final BPlusNode<K, V> childNode;

        public NodeBorrowed(BPlusNode<K, V> lendingNode, BPlusNode<K, V> borrowingNode, BPlusNode<K, V> childNode) {
            super(EventType.NODE_BORROWED);
            this.lendingNode = lendingNode;
            this.borrowingNode = borrowingNode;
            this.childNode = childNode;
        }

        public BPlusNode<K, V> getLendingNode() { return lendingNode; }
        public BPlusNode<K, V> getBorrowingNode() { return borrowingNode; }
        public BPlusNode<K, V> getChildNode() { return childNode; }
    }

    public static class NodeCreated<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> parent;
        private final BPlusNode<K, V> child;

        public NodeCreated(BPlusNode<K, V> parent, BPlusNode<K, V> child) {
            super(EventType.NODE_CREATED);
            this.parent = parent;
            this.child = child;
        }

        public BPlusNode<K, V> getParent() { return parent; }
        public BPlusNode<K, V> getChild() { return child; }
    }

    public static class NodeDeleted<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> parent;
        private final BPlusNode<K, V> child;

        public NodeDeleted(BPlusNode<K, V> parent, BPlusNode<K, V> child) {
            super(EventType.NODE_DELETED);
            this.parent = parent;
            this.child = child;
        }

        public BPlusNode<K, V> getParent() { return parent; }
        public BPlusNode<K, V> getChild() { return child; }
    }

    public static class NewRoot<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final BPlusNode<K, V> newRoot;

        public NewRoot(BPlusNode<K, V> newRoot) {
            super(EventType.NEW_ROOT);
            this.newRoot = newRoot;
        }

        public BPlusNode<K, V> getNewRoot() { return newRoot; }
    }

    public static class KeyChanged<K extends Comparable<K>, V> extends BPlusTreeEvent<K, V> {
        private final Key<K> key;
        private final K newKey;

        public KeyChanged(Key<K> key, K newKey) {
            super(EventType.KEY_CHANGED);
            this.key = key;
            this.newKey = newKey;
        }


        public Key<K> getKey() { return key; }
        public K getNewKey() { return newKey; }
    }
}
