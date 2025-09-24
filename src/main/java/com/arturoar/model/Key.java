package com.arturoar.model;

/**
 * Represents a key in the B+ Tree.
 *
 * @param <K> the type of the key, which must be comparable
 */
public class Key<K extends Comparable<K>> {

    /**
     * The key value.
     */
    public K key;

    /**
     * Constructs a new Key with the specified value.
     *
     * @param key the value of the key
     */
    Key(K key){
        this.key = key;
    }

    /**
     * Creates a clone of this key with the specified value.
     *
     * @param key the value of the new key
     * @return a new Key object with the specified value
     */
    public Key<K> clone(K key){
        return new Key<K>(key);
    }
    
    public K getKey(){
        return this.key;
    }

    public void setKey(K key){
        this.key = key;
    }
    
}
