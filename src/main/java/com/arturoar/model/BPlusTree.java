package com.arturoar.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.BPlusTreeEvent;
import com.arturoar.util.BPlusTreeObserver;

public class BPlusTree<K extends Comparable<K>,V> {

    private BPlusNode<K,V> root;
    private int B;
    private ArrayList<BPlusNode<K,V>> nodes;

    public BPlusTree(int B){
        this.B = B;
        this.root = new BPlusLeafNode<K,V>(true, B, 0, null);
        this.nodes = new ArrayList<>(List.of(this.root));
    }

    public BPlusNode<K,V> getRoot(){
        return this.root;
    }

    public int getB(){
        return this.B;
    }

    public void setRoot(BPlusNode<K,V> newRoot){
        this.root = newRoot;
        for (BPlusNode<K,V> node: this.nodes){
            if (!node.equals(this.root)){
                node.increaseLevel();
            }
        }
    }
    
    public BPlusTraversalResult<Boolean,K> insert(K key, V data){
        BPlusLeafNode<K,V> leafNode = searchLeafNode(key);
        BPlusTraversalResult<Boolean,K> containResult = this.contains(key);
        BPlusTraversalResult<Boolean,K> insertResult = new BPlusTraversalResult<>(containResult.getVisitedKeys(),false);
        if (!containResult.getResult()) {
            int keyIndex = findNodeIndex(key, leafNode);
            Key<K> newKey = new Key<K>(key);
            leafNode.addKey(keyIndex, newKey);
            leafNode.getData().add(keyIndex, data);

            //notifyObservers(new BTreeEvent<K,V>(BTreeEvent.EventType.KEY_INSERTED, leafNode, newKey));
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

    public BPlusTraversalResult<V,K> remove(K key){
        BPlusTraversalResult<Boolean,K> containsResult = this.contains(key);
        BPlusTraversalResult<V,K> removeResult = new BPlusTraversalResult<>( containsResult.getVisitedKeys());

        if (!containsResult.getResult()) {
            return null;
        }else{
            BPlusLeafNode<K,V> leafNode = searchLeafNode(key);

            int keyIndex = leafNode.getKeyValues().indexOf(key);

            Key<K> deletedKey =  leafNode.deleteKey(key);

            removeResult.setResult(leafNode.getData().remove(keyIndex));

            //notifyObservers(new BTreeEvent<K,V>(BTreeEvent.EventType.KEY_REMOVED, leafNode, deletedKey));
            notifyObservers(new BPlusTreeEvent.KeyRemoved<>(leafNode, deletedKey));

            if (!leafNode.isUnderFlow()) {    
                return removeResult;
            }
            else {
                if (leafNode == this.root) 
                    return removeResult;
                else {
                    int currentPageIndex = leafNode.getChildrenIndex();
                    int borrower = searchBorrower(currentPageIndex, leafNode.getParent());
                    if ( borrower != 0) {
                        borrowKey(leafNode, borrower);
                    }
                    else {
                        mergeNodes(leafNode);
                    }
                    return removeResult;
                }
            }   
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
    public BPlusTraversalResult<V,K> search(K key) {
        BPlusTraversalResult<V,K> result = new BPlusTraversalResult<>();
        
        if (this.root == null) {
            throw new IllegalStateException("The B+ tree is empty.");
        }
        
        ArrayDeque<BPlusNode<K,V>> nodesQueue = new ArrayDeque<>();
        nodesQueue.addLast(this.root);
        
        while (!nodesQueue.isEmpty()) {
            BPlusNode<K, V> currentNode = nodesQueue.poll();
            
            if (currentNode.isLeaf()) {
                processLeafNode(currentNode, key, result);
                if (result.getResult() != null) {
                    return result;
                }
            } else {
                int childIndex = findChildIndex(currentNode, key);
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
    private void processLeafNode(BPlusNode<K, V> currentNode, K key, BPlusTraversalResult<V,K> result) {
        for (K storedKey : currentNode.getKeyValues()) {
            if (key.compareTo(storedKey) == 0) {
                int index = currentNode.getKeyValues().lastIndexOf(key);
                result.setResult(((BPlusLeafNode<K, V>) currentNode).getData(index));
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
    private int findChildIndex(BPlusNode<K, V> currentNode, K key) {
        int childIndex = 0;
        for (K storedKey : currentNode.getKeyValues()) {
            if (key.compareTo(storedKey) <= 0) {
                childIndex++;
            } else {
                break;
            }
        }
        return childIndex;
    }



    public BPlusTraversalResult<Boolean,K> contains(K key){
        BPlusTraversalResult<Boolean,K> returnValue = new BPlusTraversalResult<>();
        
        BPlusTraversalResult<V,K> data = this.search(key);

        returnValue.setResult( data.getResult() != null );

        return returnValue;

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
            newNode = new BPlusLeafNode<>(true, this.B, node.getLevel(), node.getParent());
            ((BPlusLeafNode<K,V>)newNode).setNextLeafNode(((BPlusLeafNode<K,V>)node).getNextLeafNode());
            ((BPlusLeafNode<K,V>)node).setNextLeafNode((BPlusLeafNode<K,V>)newNode);
        }else{
            newNode = new BPlusInnerNode<>(false, this.B, node.getLevel(), node.getParent());
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
                notifyObservers(new BPlusTreeEvent.ChildNodeBorrowed<>(node, newNode, child));
            }

            for (BPlusNode<K,V> sibling : ((BPlusInnerNode<K,V>)newNode).getChildren()) {
                sibling.setParent((BPlusInnerNode<K,V>)newNode);
            }
        }
        
        if (node == this.root) {

            BPlusInnerNode<K,V> newRoot = new BPlusInnerNode<>(false, this.B, 0 , null);
            this.nodes.add(newRoot);
            setRoot(newRoot);
            node.setParent(newRoot);
            newNode.setParent(newRoot);

            notifyObservers(new BPlusTreeEvent.NewRoot<K,V>(newRoot));

            /////CHILDREN ADDED
            newRoot.getChildren().add(node);
            newRoot.getChildren().add(newNode);

            notifyObservers(new BPlusTreeEvent.ChildNodeCreated<>(newRoot,node));
            notifyObservers(new BPlusTreeEvent.ChildNodeCreated<>(newRoot,newNode));

            
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

            notifyObservers(new BPlusTreeEvent.ChildNodeCreated<>(node.getParent(),newNode));

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
     * @param node Pagina con deficit de keys.
     * @return Regresa true si se logra unir las paginas.
     */
    private void mergeNodes(BPlusNode<K,V> node) {

        int siblingIndex = node.getChildrenIndex();
        if ( siblingIndex == node.getParent().getChildren().size()-1 ){
            node = node.getParent().getChild(--siblingIndex);
        }
        BPlusNode<K,V> nextNode = node.getParent().getChild(siblingIndex+1);

        if (node.isLeaf()) {
            List<Key<K>> borrowedKeys = nextNode.getKeys();

            node.getKeys().addAll(borrowedKeys);
            notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(nextNode, node, borrowedKeys));

            ((BPlusLeafNode<K,V>)node).getData().addAll(((BPlusLeafNode<K,V>)nextNode).getData());

            ((BPlusLeafNode<K,V>)node).setNextLeafNode(((BPlusLeafNode<K,V>)nextNode).getNextLeafNode());
            
            /* 
            for (int keyIndex=0; keyIndex < nextNode.size(); keyIndex++){
                node.addKey(nextNode.getKeys().get(keyIndex));
                ((BPlusLeafNode<K,V>)node).getData().add(((BPlusLeafNode<K,V>)nextNode).getData(keyIndex));
                //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.KEY_BORROWED, nextNode ,node, nextNode.getKeys().get(keyIndex)));
            }
            */
            
            /// ELIMINATED NODE

        }
        else {
            Key<K> newKey = new Key<K>(node.getParent().getKey(siblingIndex));
            node.getKeys().add(newKey);
            //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.KEY_INSERTED, node, newKey));
            notifyObservers(new BPlusTreeEvent.KeyInserted<>(node, newKey));

            for (BPlusNode<K,V> child : ((BPlusInnerNode<K,V>)nextNode).getChildren()) {
                child.setParent((BPlusInnerNode<K,V>)node);
                ((BPlusInnerNode<K,V>)node).getChildren().add(child);
                //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.CHILDNODE_BORROWED, node, nextNode, child));
                notifyObservers(new BPlusTreeEvent.ChildNodeBorrowed<>(node, nextNode, child));
            }

            /*
            for ( Key<K> key : nextNode.getKeys()) {
                node.getKeys().add(key);
                notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.KEY_BORROWED, nextNode , node, key));
            }*/

            List<Key<K>> borrowedKeys = nextNode.getKeys();
            node.getKeys().addAll(borrowedKeys);
            notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(nextNode, node, borrowedKeys));

            if (node.getParent() == this.root && this.root.size() == 0) {
                this.root = node;
                notifyObservers(new BPlusTreeEvent.NewRoot<>(node));   //////////////////////////////////////////////
                this.nodes.remove(node.getParent());
                //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.NODE_DELETED, node.getParent()));
                notifyObservers(new BPlusTreeEvent.NodeDeleted<>(node.getParent()));

                for (BPlusNode<K,V> currentNode: this.nodes){
                    currentNode.decreaseLevel();
                }
                this.root.setParent(null);
                node = ((BPlusInnerNode<K,V>)node).getChild(0);
            }
        }
        node.getParent().getKeys().remove(siblingIndex);
        BPlusNode<K,V> childDeleted = node.getParent().getChildren().remove(siblingIndex+1);

        //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.CHILDNODE_DELETED, null, node.getParent(), childDeleted));
        notifyObservers(new BPlusTreeEvent.ChildNodeDeleted<>(node.getParent(), childDeleted));

        if (node.getParent() == this.root && node.getParent().getKeys().isEmpty()) {
            this.root = node;
            this.root.setParent(null);
        }
        else {
            if (node.getParent().isUnderFlow()) {
                int lendingNode = searchBorrower(node.getParent().getChildrenIndex(),node.getParent().getParent());
                if (lendingNode != 0) {
                    borrowKey(node.getParent(),lendingNode);
                }
                else {
                    mergeNodes(node.getParent());
                }
            }
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
                if (storedKey.compareTo(key) >= 0) {
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
                if(!leftSibling.isUnderFlow())
                    return -1;
            } else {
                if(!rightSibling.isUnderFlow()) 
                    return 1;
            }
            return 0;
        }

        // Special case for the first child: check if the right neighbor can lend keys
        if(childIndex == 0 && !parent.getChild(childIndex + 1).isUnderFlow())
            return 1;
        
        // Special case for the last child: check if the left neighbor can lend keys
        if(childIndex == parent.getChildren().size() - 1 && !parent.getChild(childIndex - 1).isUnderFlow())
            return -1;
        
        // If no suitable neighbor can lend keys, return 0
        return 0;
    }


    private void borrowKey(BPlusNode<K,V> node, int borrower) {

        int h = node.getChildrenIndex();
        BPlusNode<K,V> lendingNode = node.getParent().getChild(h + borrower);

        switch(borrower){
            case -1:
                Key<K> borrowedKey = lendingNode.getKeys().removeLast();
                node.getKeys().addFirst(borrowedKey);
                //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.KEY_BORROWED, lendingNode, node, borrowedKey));
                notifyObservers(new BPlusTreeEvent.KeyBorrowed<>(lendingNode, node, java.util.Arrays.asList(borrowedKey)));
                Key<K> createdKey = new Key<K>(node.getKey(0));
                node.getParent().getKeys().set(h-1,createdKey);
                //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.KEY_INSERTED, node.getParent(),createdKey));
                notifyObservers(new BPlusTreeEvent.KeyInserted<>(node.getParent(), createdKey));

                if (node.isLeaf()){
                    ((BPlusLeafNode<K,V>)node).getData().addFirst(((BPlusLeafNode<K,V>)lendingNode).getData().removeLast());
                }else{
                    BPlusNode<K,V> child = ((BPlusInnerNode<K,V>)lendingNode).getChildren().removeLast();
                    ((BPlusInnerNode<K,V>)node).getChildren().addFirst(child);
                    ((BPlusInnerNode<K,V>)node).getChild(0).setParent((BPlusInnerNode<K,V>)node);
                    //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.CHILDNODE_BORROWED, node, lendingNode, child));
                    notifyObservers(new BPlusTreeEvent.ChildNodeBorrowed<>(node, lendingNode, child));
                }
            case 1:        
                Key<K> borrowedKey2 = lendingNode.getKeys().removeFirst();
                node.getKeys().add(borrowedKey2); 
                //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.KEY_INSERTED, node, borrowedKey2));
                notifyObservers(new BPlusTreeEvent.KeyInserted<>(node, borrowedKey2));

                node.getParent().getKeys().set(h, borrowedKey2);
                if (node.isLeaf()){
                    ((BPlusLeafNode<K,V>) node).getData().addLast(((BPlusLeafNode<K,V>)lendingNode).getData().removeFirst());
                }else{
                    BPlusNode<K,V> child = ((BPlusInnerNode<K,V>)lendingNode).getChildren().removeFirst();
                    ((BPlusInnerNode<K,V>)node).getChildren().addLast(child);
                    //notifyObservers(new BTreeEvent<>(BTreeEvent.EventType.CHILDNODE_BORROWED, node, lendingNode, child));
                    notifyObservers(new BPlusTreeEvent.ChildNodeBorrowed<>(node, lendingNode, child));
                }
            default:
                break;
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
