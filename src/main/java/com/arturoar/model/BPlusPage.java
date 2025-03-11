package com.arturoar.model;

import java.util.ArrayList;

/**
 * Esta clase representa las paginas que contienen las claves, hijos, nodos, etc del árbol.
 * @author Aguilera Roa Mauricio Arturo
 */
public class BPlusPage {
    
    private boolean isLeaf;
    private ArrayList<BPlusNode> keys;
    private ArrayList<BPlusLeafNode> nodes;
    private BPlusPage parent;
    private ArrayList<BPlusPage> children;
    private BPlusPage nextPage;
    private int level;

    /**
     * Contruye una instancia de la clase Pagina inicializando hoj
     */
    public BPlusPage(int level) {
        this.keys = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.level = level;
    }
    
    public boolean isLeaf() {
        return isLeaf;
    }
    
    public void setLeaf(boolean hoja) {
        this.isLeaf = hoja;
    }
    /**
     * Esta clase regresa el entero guardado en la posicion i de la lista clave.
     * @param i Indice del elemento.
     * @return Entero en posicion i de clave.
     */
    public int getKey(int i) {
        return this.keys.get(i).getKey();
    }
    
    public ArrayList<BPlusNode> getKeys() {
        return this.keys;
    }

    public void setKeys(ArrayList<BPlusNode> keys) {
        this.keys = keys;
    }


    public ArrayList<BPlusLeafNode> getNodes() {
        return nodes;
    }
    /**
     * Este método regresa el indice que ocupa esta instancia en la lista de hijos de su padre.
     * @return Indice de la lista de hijos de pagina padre.
     */
    public int getChildrenIndex(){
        if( this.parent == null ){
            return -1;
        }
        else{
            BPlusPage padre = this.parent;
            for( int i = 0 ; i < padre.children.size() ; i++ ){
                if( padre.children.get(i) == this )
                    return i;
            }     
            return -1;
        }
    }
    /**
     * Este método regresa el nodo en el indice de la lista nodo. 
     * @param indice Valor del indice del nodo requerido.
     * @return Regresa el nodo en la posición indice
     */
    public BPlusLeafNode getNode(int indice) {
        return this.nodes.get(indice);
    }

    public void setNodes(ArrayList<BPlusLeafNode> nodo) {
        this.nodes = nodo;
    }

    public BPlusPage getParent() {
        return parent;
    }

    public void setParent(BPlusPage padre) {
        this.parent = padre;
    }


    public ArrayList<BPlusPage> getChildren() {
        return children;
    }
    /**
     * 
     * @param indice
     * @return 
     */
    public BPlusPage getChild(int indice) {
        return this.children.get(indice);
    }

    public void setChildren(ArrayList<BPlusPage> children) {
        this.children = children;
    }

    public BPlusPage getNextPage() {
        return nextPage;
    }

    public void setNextPage(BPlusPage nextPage) {
        this.nextPage = nextPage;
    }
    /**
     * Este método imprime en pantalla las claves de la lista clave
     */
    public void showKeys(){
        for( int i = 0 ; i < this.keys.size() ; i++ )
            System.out.print( this.keys.get(i).getKey() + " " );
    }

    public int getLevel(){
        return this.level;
    }

    public void updateLevel() {
        this.level += 1;
    }
}

