
package proyecto2edaii.arbolbmas;

import java.util.ArrayList;

/**
 * Esta clase representa las paginas que contienen las claves, hijos, nodos, etc del árbol.
 * @author Aguilera Roa Mauricio Arturo
 */
public class Pagina {
    
    private boolean hoja;
    private ArrayList<Integer> clave;
    private ArrayList<Nodo> nodo;
    private Pagina padre;
    private ArrayList<Pagina> hijo;
    private Pagina sigPagina;

    /**
     * Contruye una instancia de la clase Pagina inicializando hoj
     */
    public Pagina() {
        this.clave = new ArrayList<>();
        this.nodo = new ArrayList<>();
        this.hijo = new ArrayList<>();
        this.hoja = true;
    }
    
    public boolean esHoja() {
        return hoja;
    }
    
    public void setHoja(boolean hoja) {
        this.hoja = hoja;
    }
    /**
     * Esta clase regresa el entero guardado en la posicion i de la lista clave.
     * @param i Indice del elemento.
     * @return Entero en posicion i de clave.
     */
    public int getClave(int i) {
        return this.clave.get(i);
    }
    
    public ArrayList<Integer> getClaves() {
        return this.clave;
    }

    public void setClave(ArrayList<Integer> clave) {
        this.clave = clave;
    }


    public ArrayList<Nodo> getNodos() {
        return nodo;
    }
    /**
     * Este método regresa el indice que ocupa esta instancia en la lista de hijos de su padre.
     * @return Indice de la lista de hijos de pagina padre.
     */
    public int getIndiceHijo(){
        if( this.padre == null ){
            return -1;
        }
        else{
            Pagina padre = this.padre;
            for( int i = 0 ; i < padre.hijo.size() ; i++ ){
                if( padre.hijo.get(i) == this )
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
    public Nodo getNodo(int indice) {
        return this.nodo.get(indice);
    }

    public void setNodo(ArrayList<Nodo> nodo) {
        this.nodo = nodo;
    }

    public Pagina getPadre() {
        return padre;
    }

    public void setPadre(Pagina padre) {
        this.padre = padre;
    }


    public ArrayList<Pagina> getHijos() {
        return hijo;
    }
    /**
     * 
     * @param indice
     * @return 
     */
    public Pagina getHijo(int indice) {
        return this.hijo.get(indice);
    }

    public void setHijo(ArrayList<Pagina> hijo) {
        this.hijo = hijo;
    }

    public Pagina getSigPagina() {
        return sigPagina;
    }

    public void setSigPagina(Pagina sigPagina) {
        this.sigPagina = sigPagina;
    }
    /**
     * Este método imprime en pantalla las claves de la lista clave
     */
    public void mostrarLlaves(){
        for( int i = 0 ; i < this.clave.size() ; i++ )
            System.out.print( this.clave.get(i) + " " );
    }
}

