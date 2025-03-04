package com.arturoar.model;

import java.util.ArrayList;

/**
 * Esta clase representa las paginas que contienen las claves, hijos, nodos, etc del árbol.
 * @author Aguilera Roa Mauricio Arturo
 */
public class BPlusPage {
    
    private boolean isLeaf;
    private ArrayList<Integer> key;
    private ArrayList<BPlusNode> node;
    private BPlusPage father;
    private ArrayList<BPlusPage> children;
    private BPlusPage nextPage;

    /**
     * Contruye una instancia de la clase Pagina inicializando hoj
     */
    public BPlusPage() {
        this.key = new ArrayList<>();
        this.node = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = true;
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
        return this.key.get(i);
    }
    
    public ArrayList<Integer> getKeys() {
        return this.key;
    }

    public void setKey(ArrayList<Integer> clave) {
        this.key = clave;
    }


    public ArrayList<BPlusNode> getNodes() {
        return node;
    }
    /**
     * Este método regresa el indice que ocupa esta instancia en la lista de hijos de su padre.
     * @return Indice de la lista de hijos de pagina padre.
     */
    public int getChildrenIndex(){
        if( this.father == null ){
            return -1;
        }
        else{
            BPlusPage padre = this.father;
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
    public BPlusNode getNode(int indice) {
        return this.node.get(indice);
    }

    public void setNode(ArrayList<BPlusNode> nodo) {
        this.node = nodo;
    }

    public BPlusPage getFather() {
        return father;
    }

    public void setFather(BPlusPage padre) {
        this.father = padre;
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

    public void setChildren(ArrayList<BPlusPage> hijo) {
        this.children = hijo;
    }

    public BPlusPage getNextPage() {
        return nextPage;
    }

    public void setNextPage(BPlusPage sigPagina) {
        this.nextPage = sigPagina;
    }
    /**
     * Este método imprime en pantalla las claves de la lista clave
     */
    public void showKeys(){
        for( int i = 0 ; i < this.key.size() ; i++ )
            System.out.print( this.key.get(i) + " " );
    }
}

