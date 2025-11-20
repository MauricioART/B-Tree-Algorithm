package com.arturoar.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.BPlusTreeEvent;
import com.arturoar.util.BPlusTreeObserver;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


public class BPlusTree<K extends Comparable<K>,V> {

    private BPlusNode<K,V> root;
    private int m;
    private ArrayList<BPlusNode<K,V>> nodes;
    private BooleanProperty emptyProperty = new SimpleBooleanProperty(true);

    public BPlusTree(int m){
        this.m = m;
    }

    public BPlusNode<K,V> getRoot(){
        return this.root;
    }

    public int getM(){
        return this.m;
    }

    public void setRoot(BPlusNode<K,V> newRoot){
        this.root = newRoot;
        for (BPlusNode<K,V> node: this.nodes){
            if (!node.equals(this.root)){
                node.increaseLevel();
            }
        }
    }

    public BooleanProperty emptyProperty(){
        return emptyProperty;
    }
    
    public BPlusTraversalResult<Boolean,K,V> insert(K key, V data){
        if (root == null){
            this.root = new BPlusLeafNode<K,V>(true, m, 0, null);
            this.nodes = new ArrayList<>(List.of(this.root));
            notifyObservers(new BPlusTreeEvent.NewRoot<>(root));
            emptyProperty.set(false);
        }
        BPlusLeafNode<K,V> leafNode = searchLeafNode(key);
        BPlusTraversalResult<BPlusLeafNode<K, V>, K, V> containResult = this.contains(key);
        BPlusTraversalResult<Boolean, K, V> insertResult = new BPlusTraversalResult<>(containResult.getVisitedKeys(), false);
        if (containResult.getResult() == null ) {
            int keyIndex = findNodeIndex(key, leafNode);
            Key<K> newKey = new Key<K>(key);
            leafNode.addKey(keyIndex, newKey);
            leafNode.getData().add(keyIndex, data);

            notifyObservers(new BPlusTreeEvent.KeyInserted<>(leafNode, newKey));

            insertResult.setResult(true);

            if(!leafNode.isOverFlow()){
                return insertResult;
            }else {
                splitNode(leafNode);
                return insertResult;
            }
        }else
            return insertResult;
    } 
 
    public BPlusTraversalResult<Key<K>, K, V> remove(K key){

        BPlusTraversalResult<BPlusLeafNode<K, V>, K, V> containsResult = this.contains(key);
        BPlusTraversalResult<Key<K>, K, V> removeResult = new BPlusTraversalResult<>( containsResult.getVisitedKeys());

        if (containsResult.getResult() != null) {
            
            BPlusLeafNode<K,V> leafNode =  containsResult.getResult();

            int keyIndex = leafNode.getKeyValues().indexOf(key);

            Key<K> deletedKey =  leafNode.deleteKey(key);
            leafNode.getData().remove(keyIndex);

            
            removeResult.setResult(deletedKey);
            
            notifyObservers(new BPlusTreeEvent.KeyRemoved<>(leafNode, deletedKey));
            
            
            if (leafNode.isUnderFlow()) 
                handleUnderFlow(leafNode);
            
            if (leafNode == root && root.isEmpty()){
                root = null;
                emptyProperty.set(true);
            }
        }

        return removeResult;

    }

    private void handleUnderFlow(BPlusNode<K,V> node){
        if (node == this.root) return;

        int currentPageIndex = node.getChildrenIndex();
        int borrower = searchBorrower(currentPageIndex, node.getParent());
        
        switch (borrower) {
            case -1:
                borrowFromLeft(node);
                break;
            case 0:
                mergeNodes(node);
                break;
            case 1:
                borrowFromRight(node);
                break;
            default:
                break;
        }

    }

    private int findNodeIndex(K key, BPlusLeafNode<K,V> currentNode){
        int keyIndex = 0;
        for (K storedKey : currentNode.getKeyValues()) {
            if (storedKey.compareTo(key) <= 0)
                keyIndex++;
            else 
                break;
        } 
        return keyIndex;
    }

    /**
     * Searches for a key in the B+ tree.
     * 
     * @param key The key to search for.
     * @return A BPlusTraversalResult containing the data associated with the key if found.
     */
    public BPlusTraversalResult<BPlusLeafNode<K, V>, K, V> search(K key) {
        BPlusTraversalResult<BPlusLeafNode<K, V>, K, V> result = new BPlusTraversalResult<>();

        if (this.root == null) {
            throw new IllegalStateException("The B+ tree is empty.");
        }
        
        ArrayDeque<BPlusNode<K,V>> nodesQueue = new ArrayDeque<>();
        nodesQueue.addLast(this.root);
        
        while (!nodesQueue.isEmpty()) {
            BPlusNode<K, V> currentNode = nodesQueue.poll();
            //result.addVisitedKey(currentNode);
            
            if (currentNode.isLeaf()) {
                processLeafNode(currentNode, key, result);
                if (result.getResult() != null) {
                    return result;
                }
            } else {
                int childIndex = findChildIndex(currentNode, key, result.getVisitedKeys());
                nodesQueue.addLast(((BPlusInnerNode<K, V>) currentNode).getChild(childIndex));
            }
        }
        
        return result;
    }

    /**
     * Processes the leaf node to search for the key and set the result if found.
     * 
     * @param currentNode The current leaf node.
     * @param key The key to search for.
     * @param result The result that will contain the data if the key is found.
     */
    private void processLeafNode(BPlusNode<K, V> currentNode, K key, BPlusTraversalResult<BPlusLeafNode<K, V>, K, V> result) {
        for (Key<K> storedKey : currentNode.getKeys()) {
            result.addVisitedKey(storedKey);
            if (key.compareTo(storedKey.key) < 0) {
                break;
            }
            if (key.compareTo(storedKey.key) == 0) {
                result.setResult(((BPlusLeafNode<K, V>) currentNode));
                break;
            }
        }
    }

    /**
     * Finds the index of the child node to traverse based on the key.
     * 
     * @param currentNode The current internal node.
     * @param key The key to search for.
     * @return The index of the child node to traverse.
     */
    private int findChildIndex(BPlusNode<K, V> currentNode, K key, List<Key<K>> visitedKeys) {
        int childIndex = 0;
        for (Key<K> storedKey : currentNode.getKeys()) {
            visitedKeys.add(storedKey);
            if (key.compareTo(storedKey.key) >= 0) {
                childIndex++;
            } else {
                break;
            }
        }
        return childIndex;
    }   



    public BPlusTraversalResult<BPlusLeafNode<K, V>, K, V> contains(K key){
        return this.search(key);

    }

     /**
     * Método auxiliar de insertNode
     . Se encarga de crear una nueva pagina si la pagina actual está por encima
     * del tope de su capacidad y reparte las keys y/o nodos entre ambas paginas.
     * @param key Parámetro con el valor a insertar en la pagina.
     * @param node Pagina actual donde se quiere insertar key
     */
    private void splitNode(BPlusNode<K,V> node) {

        BPlusNode<K,V> newNode;
        int size = node.size();

        // New node creation
        if (node.isLeaf()){
            newNode = new BPlusLeafNode<>(true, this.m, node.getLevel(), node.getParent());
            ((BPlusLeafNode<K,V>)newNode).setNextLeafNode(((BPlusLeafNode<K,V>)node).getNextLeafNode());
            ((BPlusLeafNode<K,V>)node).setNextLeafNode((BPlusLeafNode<K,V>)newNode);
        }else{
            newNode = new BPlusInnerNode<>(false, this.m, node.getLevel(), node.getParent());
        }
        this.nodes.add(newNode);

        // Keys and/or children redistribution
        ArrayList<Key<K>> leftKeys = new ArrayList<>(node.getKeys().subList(0, size/2));
        ArrayList<Key<K>> rightKeys = new ArrayList<>(node.getKeys().subList(size/2, size));
        
        node.setKeys(leftKeys);
        newNode.setKeys(rightKeys);
        
        notifyObservers(new BPlusTreeEvent.NodeSplit<K,V>(node,newNode,rightKeys.get(0)));

        notifyObservers(new BPlusTreeEvent.KeyBorrowed<K,V>(node, newNode, rightKeys));
        

        if (node.isLeaf()) {

            ArrayList<V> leftData = new ArrayList<>(((BPlusLeafNode<K,V>)node).getData().subList(0, size/2));
            ArrayList<V> rightData = new ArrayList<>(((BPlusLeafNode<K,V>)node).getData().subList(size/2, size));

            ((BPlusLeafNode<K,V>)node).setData(leftData);
            ((BPlusLeafNode<K,V>)newNode).setData(rightData);

        }
        else {

            int numOfChildren = newNode.size();
            for (int j = 0; j < numOfChildren; j++){
                BPlusNode<K,V> child = ((BPlusInnerNode<K,V>)node).getChildren().removeLast();
                ((BPlusInnerNode<K,V>)newNode).getChildren().addFirst(child);
                notifyObservers(new BPlusTreeEvent.NodeBorrowed<>(node, newNode, child));
            }

            for (BPlusNode<K,V> sibling : ((BPlusInnerNode<K,V>)newNode).getChildren()) {
                sibling.setParent((BPlusInnerNode<K,V>)newNode);
            }
        }
        
        if (node == this.root) {

            BPlusInnerNode<K,V> newRoot = new BPlusInnerNode<>(false, this.m, 0 , null);
            this.nodes.add(newRoot);
            setRoot(newRoot);
            node.setParent(newRoot);
            newNode.setParent(newRoot);

            notifyObservers(new BPlusTreeEvent.NewRoot<K,V>(newRoot));

            /////CHILDREN ADDED
            newRoot.getChildren().add(node);
            newRoot.getChildren().add(newNode);

            notifyObservers(new BPlusTreeEvent.NodeCreated<>(newRoot,node));
            notifyObservers(new BPlusTreeEvent.NodeCreated<>(newRoot,newNode));

            
            if (node.isLeaf()){
                Key<K> newKey = new Key<K>(newNode.getKey(0));
                newRoot.getKeys().add(newKey);
                notifyObservers(new BPlusTreeEvent.KeyInserted<>(newRoot,newKey)); 
             }else{
                Key<K> borrowedKey = newNode.getKeys().removeFirst();
                 newRoot.getKeys().add(borrowedKey);  ///CHECK THIS PART
                 notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(newNode, newRoot, java.util.Arrays.asList(borrowedKey)));
             }
        }
        else {
            int currentNodeIndex = node.getChildrenIndex();
            node.getParent().addChild(currentNodeIndex + 1, newNode);

            notifyObservers(new BPlusTreeEvent.NodeCreated<>(node.getParent(),newNode));

            if (node.isLeaf()){
                Key<K> newKey = new Key<K>(newNode.getKey(0));
                node.getParent().getKeys().add(currentNodeIndex, newKey);
                notifyObservers(new BPlusTreeEvent.KeyInserted<>(node.getParent(),newKey));
            }else{
                Key<K> keyBorrowed = newNode.getKeys().removeFirst();
                node.getParent().getKeys().add(currentNodeIndex, keyBorrowed);
                notifyObservers(new BPlusTreeEvent.KeyBorrowed<K, V>(newNode, node.getParent(), java.util.Arrays.asList(keyBorrowed)));
            }

            if (node.getParent().isOverFlow()) {
                splitNode(node.getParent());
            }

            
        }
    }

    /**
     * Método auxiliar de removeNode. Se encarga de unir paginas vecinas debido a un deficit de keys 
     * en pagActual y no hay nodos vecinos capacez de prestar keys.
     * @param leftNode Pagina con deficit de keys.
     * @return Regresa true si se logra unir las paginas.
     */
    private void mergeNodes(BPlusNode<K,V> leftNode) {

        int nodeIndex = leftNode.getChildrenIndex();
        if ( nodeIndex == leftNode.getParent().getChildren().size()-1 ){
            leftNode = leftNode.getParent().getChild(--nodeIndex);
        }
        BPlusNode<K,V> rightNode = leftNode.getParent().getChild(nodeIndex+1);

        List<Key<K>> rightKeys = rightNode.getKeys();

        if (leftNode.isLeaf()){
            
            ((BPlusLeafNode<K,V>)leftNode).getData().addAll(((BPlusLeafNode<K,V>)rightNode).getData());
            ((BPlusLeafNode<K,V>)leftNode).setNextLeafNode(((BPlusLeafNode<K,V>)rightNode).getNextLeafNode());

        }else{
            
            Key<K> newKey = new Key<K>(leftNode.getParent().getKey(nodeIndex));
            leftNode.getKeys().add(newKey);
            notifyObservers(new BPlusTreeEvent.KeyInserted<>(leftNode, newKey));
            
            for (BPlusNode<K,V> child : ((BPlusInnerNode<K,V>)rightNode).getChildren()) {
                child.setParent((BPlusInnerNode<K,V>)leftNode);
                ((BPlusInnerNode<K,V>)leftNode).getChildren().add(child);
                notifyObservers(new BPlusTreeEvent.NodeBorrowed<>(leftNode, rightNode, child));
            }
        }

        leftNode.getKeys().addAll(rightKeys);
        
        notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(rightNode, leftNode, rightKeys));
        
        Key<K> keyRemoved = leftNode.getParent().getKeys().remove(nodeIndex);
        notifyObservers(new BPlusTreeEvent.KeyRemoved<>(leftNode.getParent(), keyRemoved));
        
        BPlusNode<K,V> nodeDeleted = leftNode.getParent().getChildren().remove(nodeIndex+1);
        notifyObservers(new BPlusTreeEvent.NodeDeleted<>(leftNode.getParent(), nodeDeleted));

        if (leftNode.getParent() == root && root.isEmpty()){

            this.root = leftNode;
            notifyObservers(new BPlusTreeEvent.NewRoot<>(leftNode));   
            this.nodes.remove(leftNode.getParent());

    
            for (BPlusNode<K,V> currentNode: this.nodes){
                currentNode.decreaseLevel();
            }
            this.root.setParent(null);

        }else if (leftNode.getParent().isUnderFlow()){
            handleUnderFlow(leftNode.getParent());
        }
    
    }
    

    /**
     * Searches for the leaf node that contains the specified key.
     * This method initiates the recursive search starting from the root node.
     *
     * @param key The key to search for in the B+ tree.
     * @return The leaf node containing the specified key, or null if not found.
     */
    private BPlusLeafNode<K,V> searchLeafNode(K key) {
        return searchLeafNodeRecursive(this.root, key);
    }

    /**
     * Recursively searches through the B+ tree to find the leaf node that contains the specified key.
     * This method is called recursively for internal nodes until a leaf node is found.
     *
     * @param currentNode The current node being examined in the tree.
     * @param key The key to search for in the B+ tree.
     * @return The leaf node containing the specified key.
     */

    private BPlusLeafNode<K,V> searchLeafNodeRecursive(BPlusNode<K,V> currentNode, K key) {
    
        if (currentNode.isLeaf()) {
            return (BPlusLeafNode<K,V>) currentNode;
        } else {
            int childIndex = 0;

            for (K storedKey : currentNode.getKeyValues()) {
                if (storedKey.compareTo(key) > 0) {
                    break;
                }
                childIndex++; 
            }

            return searchLeafNodeRecursive(((BPlusInnerNode<K,V>) currentNode).getChild(childIndex), key);
        }
    }

    /**
     * This method is responsible for finding a neighboring page capable of lending keys.
     * @param childIndex The index of the page with a key deficit in the parent's child list.
     * @param parent The parent page of the page with the key deficit.
     * @return Returns -1 if the found page is the left neighbor, 1 if it's the right neighbor,
     * and 0 if neither neighbor can lend keys.
     */
    private int searchBorrower(int childIndex, BPlusInnerNode<K,V> parent){
        
        if(childIndex > 0 && childIndex < parent.getChildren().size() - 1){

            BPlusNode<K,V> leftSibling = parent.getChild(childIndex - 1); 
            BPlusNode<K,V> rightSibling = parent.getChild(childIndex + 1); 
        
            if (leftSibling.size() >= rightSibling.size()){
                if(!leftSibling.wouldBeUnderFlow())
                    return -1;
            } else {
                if(!rightSibling.wouldBeUnderFlow())
                    return 1;
            }
            return 0;
        }

        // Special case for the first child: check if the right neighbor can lend keys
        if(childIndex == 0 && !parent.getChild(childIndex + 1).wouldBeUnderFlow())
            return 1;
        
        // Special case for the last child: check if the left neighbor can lend keys
        if(childIndex == parent.getChildren().size() - 1 && !parent.getChild(childIndex - 1).wouldBeUnderFlow())
            return -1;
        
        // If no suitable neighbor can lend keys, return 0
        return 0;
    }


    private void borrowFromLeft(BPlusNode<K,V> rightNode){
        int nodeIndex = rightNode.getChildrenIndex();
        BPlusNode<K,V> leftNode = rightNode.getParent().getChild(nodeIndex - 1);
        Key<K> borrowedKey = leftNode.getKeys().removeLast();
        
        rightNode.addKey(0, borrowedKey);
        notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(leftNode, rightNode, java.util.Arrays.asList(borrowedKey)));
        

        if (rightNode.isLeaf()){

            K newValue = borrowedKey.getKey();
            Key<K> updatedKey = rightNode.getParent().getKeys().get(nodeIndex - 1);
            updatedKey.setKey(newValue);
            notifyObservers(new BPlusTreeEvent.KeyChanged<K,V>(updatedKey, newValue));

            ((BPlusLeafNode<K,V>)rightNode).getData().addFirst(((BPlusLeafNode<K,V>)leftNode).getData().removeLast());


        }else{
            
            Key<K> separatingKey = rightNode.getParent().getKeys().get(nodeIndex - 1);
            K bufferSepKey = separatingKey.getKey();

            separatingKey.setKey(borrowedKey.getKey());
            notifyObservers(new BPlusTreeEvent.KeyChanged<>(separatingKey, borrowedKey.getKey()));

            borrowedKey.setKey(bufferSepKey);
            notifyObservers(new BPlusTreeEvent.KeyChanged<>(borrowedKey, bufferSepKey));


            BPlusNode<K,V> child = ((BPlusInnerNode<K,V>)leftNode).getChildren().removeLast();
            ((BPlusInnerNode<K,V>)rightNode).getChildren().addFirst(child);
            ((BPlusInnerNode<K,V>)rightNode).getFirstChild().setParent((BPlusInnerNode<K,V>)rightNode);
            notifyObservers(new BPlusTreeEvent.NodeBorrowed<>(leftNode, rightNode, child));
        }
    }
    
    private void borrowFromRight(BPlusNode<K,V> leftNode){
        int nodeIndex = leftNode.getChildrenIndex();
        BPlusNode<K,V> rightNode = leftNode.getParent().getChild(nodeIndex + 1);
        Key<K> borrowedKey = rightNode.getKeys().removeFirst();
        
        leftNode.getKeys().addLast(borrowedKey);
        notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(rightNode, leftNode, java.util.Arrays.asList(borrowedKey)));
       
        if (leftNode.isLeaf()){

            K newValue = rightNode.keys.get(0).getKey();
            Key<K> updatedKey = leftNode.getParent().getKeys().get(nodeIndex);
            updatedKey.setKey(newValue);
            notifyObservers(new BPlusTreeEvent.KeyChanged<K,V>(updatedKey, newValue));

            ((BPlusLeafNode<K,V>) leftNode).getData().addLast(((BPlusLeafNode<K,V>)rightNode).getData().removeFirst());


        }else{

            Key<K> separatingKey = rightNode.getParent().getKeys().get(nodeIndex);
            K bufferSepKey = separatingKey.getKey();
            
            separatingKey.setKey(borrowedKey.getKey());
            notifyObservers(new BPlusTreeEvent.KeyChanged<>(separatingKey, borrowedKey.getKey()));

            borrowedKey.setKey(bufferSepKey);
            notifyObservers(new BPlusTreeEvent.KeyChanged<>(borrowedKey, bufferSepKey));

            BPlusNode<K,V> child = ((BPlusInnerNode<K,V>)rightNode).getChildren().removeFirst();
            ((BPlusInnerNode<K,V>)leftNode).getChildren().addLast(child);
            ((BPlusInnerNode<K,V>)leftNode).getLastChild().setParent((BPlusInnerNode<K,V>)leftNode);
            notifyObservers(new BPlusTreeEvent.NodeBorrowed<>(rightNode, leftNode, child));
        }
    }

   
    public void showTree(){
        System.out.println("◆◆◆◆◆◆◆◆◆◆◆ Árbol ◆◆◆◆◆◆◆◆◆◆◆");
        /*
        if(((BPlusInternalNode<K,V>) this.root).getChildren().isEmpty()==true && this.root.getKeys().isEmpty()==true){
            System.out.println("No hay elementos aun");
            System.out.println(toString());
            return ;
        }*/
        Queue<BPlusNode<K,V>> nodes = new LinkedList<>();
        nodes.add(this.root);
        BPlusNode<K,V> padre=null;
        while( !nodes.isEmpty() ){
            
            BPlusNode<K,V> v = nodes.poll();
            if(v.getParent()==null){
                System.out.print("Nodo root: ");
            }
            if(padre!=v.getParent()){
                System.out.print("\n\n\nNodo Padre: ");
                v.getParent().showKeys();
                padre=v.getParent();
                System.out.print("\n\t\tNodos:");
            }
            System.out.print("\n\t\t");
            v.showKeys();
            if (!v.isLeaf()){
                nodes.addAll(((BPlusInnerNode<K,V>)v).getChildren());
            }
        }
        System.out.println("\n");
        System.out.println(toString());
    }


    private ArrayList<BPlusTreeObserver<K, V>> observers = new ArrayList<>();

    public void addObserver(BPlusTreeObserver<K, V> observer) {
        observers.add(observer);
    }

    public void removeObserver(BPlusTreeObserver<K, V> observer) {
        observers.remove(observer);
    }

    private void notifyObservers(BPlusTreeEvent<K,V> event) {
        for (BPlusTreeObserver<K, V> observer : observers) {
            observer.onTreeChanged(event);
        }
    }
}
