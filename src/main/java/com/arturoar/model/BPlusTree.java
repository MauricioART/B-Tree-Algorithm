package com.arturoar.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Está clase representa la estructura de datos no lineal conocida como
 * Árbol B+, contiene los métodos basicos de un árbol como inserción,
 * eliminación y busqueda.
 * @author Aguilera Roa Mauricio Arturo
 */
public class BPlusTree {

    private BPlusPage root;
    private int B;

    /**
     * Construye una instancia de clase estableciendo el parametro B.
     * @param B Parámetro de mínimo numero de hijos en pagina intermedia.
     */
    public BPlusTree(int B) {
        this.root = new BPlusPage();
        int MAX_B_SIZE = 10;
        int MIN_B_SIZE = 2;
        if (B > MAX_B_SIZE)
            this.B = MAX_B_SIZE;
        else
            this.B = B;
    }
    /**
     * Este método agrega un nodo al árbol con el parámetro key.
     * @param key Sirve para ordenar un nuevo nodo al árbol. 
     * @return True si se logro realizar la inserción y false en caso contrario
     */
    public boolean insertNode(int key, String data1) {
        BPlusPage pagInsercion = searchPage(key);
        if (!contains(key)) {
            if (pagInsercion.getKeys().size() < 2*B-1) {
                int i = 0;
                for ( int x : pagInsercion.getKeys()) {
                    if (key > x)
                        i++;
                    else
                        break;
                }
                pagInsercion.getKeys().add(i, key);
                
                BPlusNode nuevoNodo = new BPlusNode(data1);
                pagInsercion.getNodes().add(i, nuevoNodo);
                return true;
            }
            else {
                return celularDivision
        (key, pagInsercion);
                
            }
        }
        else
            return false;
    }

    public BPlusPage getRoot() {
        return root;
    }
    /**
     * Método auxiliar de insertNode
     . Se encarga de crear una nueva pagina si la pagina actual está por encima
     * del tope de su capacidad y reparte las keys y/o nodos entre ambas paginas.
     * @param key Parámetro con el valor a insertar en la pagina.
     * @param currentPage Pagina actual donde se quiere insertar key
     */
    private boolean celularDivision(int key, BPlusPage currentPage) {
        BPlusPage nuevaPagina = new BPlusPage();
        int i = 0;
        for (int x : currentPage.getKeys()) {
            if (key > x)
                i++;
            else
                break;
        }
        if (currentPage.isLeaf()) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Ingresa nombre: ");
            String dato1 = sc.nextLine();
            BPlusNode nuevoNodo = new BPlusNode(dato1);
            currentPage.getNodes().add(i, nuevoNodo);
            currentPage.getKeys().add(i, key);
            int numNodos = currentPage.getNodes().size();
            int numkeys = currentPage.getKeys().size();
            ArrayList<BPlusNode> SubListaIzqN = new ArrayList(currentPage.getNodes().subList(0, numNodos/2));
            ArrayList<BPlusNode> SubListaDerN = new ArrayList(currentPage.getNodes().subList(numNodos/2, numNodos));
            ArrayList<Integer> SubListaIzqC = new ArrayList(currentPage.getKeys().subList(0, numkeys/2));
            ArrayList<Integer> SubListaDerC = new ArrayList(currentPage.getKeys().subList(numkeys/2, numkeys));
            currentPage.setNode(SubListaIzqN);
            nuevaPagina.setNode(SubListaDerN);
            currentPage.setKey(SubListaIzqC);
            nuevaPagina.setKey(SubListaDerC);
            nuevaPagina.setNextPage(currentPage.getNextPage());
            currentPage.setNextPage(nuevaPagina);
        }
        else {
            nuevaPagina.setLeaf(false);
            currentPage.getKeys().add(i, key);
            int numkeys = currentPage.getKeys().size();
            ArrayList<Integer> SubListaIzq = new ArrayList(currentPage.getKeys().subList(0, numkeys/2));
            ArrayList<Integer> SubListaDer = new ArrayList(currentPage.getKeys().subList(numkeys/2, numkeys));
            currentPage.setKey(SubListaIzq);
            nuevaPagina.setKey(SubListaDer);
            int numHijos = numkeys + 1;
            for (int j = 0; j < B; j++)
                nuevaPagina.getChildren().add(0,currentPage.getChildren().remove(--numHijos));
            for (BPlusPage x : nuevaPagina.getChildren()) {
                x.setFather(nuevaPagina);
            }
        }
        
        if (currentPage == this.root) {
            BPlusPage nuevaPagroot = new BPlusPage();
            this.root = nuevaPagroot;
            this.root.setLeaf(false);
            currentPage.setFather(nuevaPagroot);
            nuevaPagina.setFather(nuevaPagroot);
            nuevaPagroot.getChildren().add(currentPage);
            nuevaPagroot.getChildren().add(nuevaPagina);
            if (currentPage.isLeaf())
                nuevaPagroot.getKeys().add(nuevaPagina.getKey(0));
            else
                nuevaPagroot.getKeys().add(nuevaPagina.getKeys().removeFirst());
            return true;
        }
        else {
            int indicePagActual = currentPage.getChildrenIndex();
            nuevaPagina.setFather(currentPage.getFather());
            currentPage.getFather().getChildren().add(indicePagActual + 1, nuevaPagina);
            if (currentPage.getFather().getKeys().size() < 2*B-1) {
                if (currentPage.isLeaf())
                    currentPage.getFather().getKeys().add(indicePagActual, nuevaPagina.getKey(0));
                else
                    currentPage.getFather().getKeys().add(indicePagActual, nuevaPagina.getKeys().remove(0));
                return true;
            }
            else {
                if (currentPage.isLeaf())
                    return celularDivision
            (nuevaPagina.getKey(0), currentPage.getFather());
                else
                    return celularDivision
            (nuevaPagina.getKeys().removeFirst(), currentPage.getFather());
            }
        }
    }
    /**
     * Este método elimina el nodo que está guardado en el árbol con el valor key
     * @param key Valor con el que está guardado el nodo a eliminar.
     * @return True si key se encuentra en el arbol y false en caso contrario 
     */
    public boolean removeNode(int key){
        if (contains(key)) {
            BPlusPage currentPage = searchPage(key);
            int i = 0;
            for (int x : currentPage.getKeys()) {
                if (key > x)
                    i++;
                else 
                    break;
            }
            currentPage.getKeys().remove(i);
            currentPage.getNodes().remove(i);
            if (currentPage.getKeys().size() >= B-1) {    
                return true;
            }
            else {
                if (currentPage == this.root) 
                    return true;
                else {
                    int indicePagActual = currentPage.getChildrenIndex();
                    int prestador = searchBorrower(indicePagActual, currentPage.getFather());
                    if ( prestador != 0) {
                        return borrowKey(currentPage, prestador);
                    }
                    else {
                        return mergePages(currentPage);
                    }
                }
            }   
        }
        else
            return false;
    }
    /**
     * Método auxiliar de removeNode. Se encarga de realizar las rotaciones de keys y/o nodos
     * cuando una pagina se encuentra con un menor número de keys y/o nodos y existe algúna
     * pagina vecina con suficientes para prestar.
     * @param pagActual Representa la pagina con un deficit de keys.
     * @param prestador Valor que representa que pagina vecina va a prestar. Si se trata del vecino
     * derecho su valor será de 1 y si es el izquierdo sera -1.
     */
    private boolean borrowKey(BPlusPage pagActual, int prestador) {
        int h = pagActual.getChildrenIndex();
        BPlusPage pagPrestadora = pagActual.getFather().getChild(h + prestador);
        if (prestador == 1) {
            if (pagActual.isLeaf()) {
                pagActual.getKeys().add(pagPrestadora.getKeys().removeFirst());
                pagActual.getNodes().add(pagPrestadora.getNodes().removeFirst());
                pagActual.getFather().getKeys().set(h, pagPrestadora.getKey(0));
            }
            else {
                pagActual.getKeys().add(pagActual.getFather().getKey(h));
                pagActual.getFather().getKeys().set(h, pagPrestadora.getKeys().removeFirst());
                pagActual.getChildren().add(pagPrestadora.getChildren().removeFirst());
            }
        }
        else {
            int numkeysPrestador = pagPrestadora.getKeys().size();
            if (pagActual.isLeaf()) {
                pagActual.getKeys().add(0, pagPrestadora.getKeys().remove(numkeysPrestador-1));
                pagActual.getNodes().add(0, pagPrestadora.getNodes().remove(numkeysPrestador-1));
                pagActual.getFather().getKeys().set(h-1, pagActual.getKey(0));
            }
            else {
                pagActual.getKeys().add(pagActual.getFather().getKey(h-1));
                pagActual.getFather().getKeys().set(h-1, pagPrestadora.getKeys().remove(numkeysPrestador-1));
                pagActual.getChildren().add(0, pagPrestadora.getChildren().remove(numkeysPrestador));
                pagActual.getChild(0).setFather(pagActual);
            }
        }
        return true;
    }
    /**
     * Método auxiliar de removeNode. Se encarga de unir paginas vecinas debido a un deficit de keys 
     * en pagActual y no hay nodos vecinos capacez de prestar keys.
     * @param pagActual Pagina con deficit de keys.
     * @return Regresa true si se logra unir las paginas.
     */
    private boolean mergePages(BPlusPage pagActual) {
        int h = pagActual.getChildrenIndex();
        if ( h == pagActual.getFather().getChildren().size()-1){
            pagActual = pagActual.getFather().getChild(h-1);
            h--;
        }
        BPlusPage pagSiguiente = pagActual.getFather().getChild(h+1);
        if (pagActual.isLeaf()) {
            for ( BPlusNode x : pagSiguiente.getNodes()) {
                pagActual.getNodes().add(x);
            }
            for (int x : pagSiguiente.getKeys()) {
                pagActual.getKeys().add(x);
            }
            pagActual.setNextPage(pagSiguiente.getNextPage());
        }
        else {
            for (BPlusPage x : pagSiguiente.getChildren()) {
                x.setFather(pagActual);
                pagActual.getChildren().add(x);
            }
            pagActual.getKeys().add(pagActual.getFather().getKey(h));
            for (int x : pagSiguiente.getKeys()) {
                pagActual.getKeys().add(x);
            }
            if (pagActual.getFather() == this.root && this.root.getKeys().size() == 0) {
                this.root = pagActual;
                this.root.setFather(null);
                pagActual = pagActual.getChild(0);
            }
        }
        pagActual.getFather().getKeys().remove(h);
        pagActual.getFather().getChildren().remove(h+1);
        if (pagActual.getFather() == this.root && pagActual.getFather().getKeys().isEmpty()) {
            this.root = pagActual;
            this.root.setFather(null);
            return true;
        }
        else {
            if (pagActual.getFather().getKeys().size() < B-1) {
                int prestador = searchBorrower(pagActual.getFather().getChildrenIndex(),pagActual.getFather().getFather());
                if (prestador != 0) {
                    return borrowKey(pagActual.getFather(),prestador);
                }
                else {
                    return mergePages(pagActual.getFather());
                }
            }
            else {
                return true;
            }
        }
    }
    /**
     * Este método se encarga de buscar una pagina vecina capaz de prestar keys.
     * @param indiceHijo Indice que tiene la pagina con deficit de keys en la lista de hijos
     * de su pagina padre.
     * @param padre Pagina padre de la pagina con deficit de keys.
     * @return Regresa -1 si la pagina encontrada es el vecino izquierdo, 1 si es el derecho y 0 
     * si no hay ninguna capaz de prestar.
     */
    private int searchBorrower(int indiceHijo, BPlusPage padre){
        if(indiceHijo > 0 && indiceHijo < padre.getChildren().size()-1){
            int numkeysIzq = padre.getChild(indiceHijo - 1).getKeys().size();
            int numkeysDer = padre.getChild(indiceHijo + 1).getKeys().size();
            if (numkeysIzq >= numkeysDer){
                if(numkeysIzq > B-1)
                    return - 1;
            }else{
                if(numkeysDer > B-1)
                    return 1;
            }
            return 0; 
        }
        if(indiceHijo == 0 && padre.getChild(indiceHijo + 1).getKeys().size() > B-1)
            return 1;
        if(indiceHijo == padre.getChildren().size()-1 && padre.getChild(indiceHijo - 1).getKeys().size() > B-1)
            return -1;
        return 0;        
    }
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key.
     * @param key Valor relacionado del nodo a buscar.
     * @return True si se encuntra en la estructura y false en caso contrario.
     */
    public boolean contains(int key){
        return contains(key, this.root);
    }
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key, usando recursividad.
     * @param key Valor relacionado del nodo a buscar.
     * @param pagActual Representa la pagina en la que busca la key en cierta llamada recursiva.
     * @return True si el key se encontro y false en caso contrario.
     */
    private boolean contains(int key, BPlusPage pagActual){
            int i = 0;
            for (int x : pagActual.getKeys()) {
                if ( key >= x )
                    i++;
                else 
                    break;
            }
            if (pagActual.isLeaf()) {
                for (int x : pagActual.getKeys()) {
                    if (key == x)
                        return true;
                }
                return false;
            }
            else
                return contains(key, pagActual.getChild(i));
    }
    
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key.
     * @param key Valor relacionado del nodo a buscar.
     * @return 
     */
    public BPlusNode searchKey(int key){
        return searchKey(key, this.root);
    }
    
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key, usando recursividad.
     * @param key Valor relacionado del nodo a buscar.
     * @param currentPage Representa la pagina en la que busca la key en cierta llamada recursiva.
     * @return 
     */
    private BPlusNode searchKey(int key, BPlusPage currentPage){
        int i = 0;
        for (int x : currentPage.getKeys()) {
            if ( key >= x )
                i++;
            else 
                break;
        }
        if (currentPage.isLeaf()) {
            if (currentPage.getKeys().isEmpty())
                return null;
            if (currentPage.getKey(i-1) == key)
                return currentPage.getNode(i-1);
            else
                return null;
        }
        else
            return searchKey(key, currentPage.getChild(i));
     
    }
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key.
     * @param key Valor relacionado del nodo a buscar.
     * @return Regresa la pagina en donde se encuntra el nodo.
     */
    public BPlusPage searchPage(int key){
        return searchPage(key, this.root);
    }
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key, usando recursividad.
     * @param key Valor relacionado del nodo a buscar.
     * @param currentPage Representa la pagina en la que busca la key en cierta llamada recursiva.
     * @return Regresa la pagina en donde se encuntra el nodo.
     */
    private BPlusPage searchPage(int key, BPlusPage currentPage){
            int i = 0;
            for (int x : currentPage.getKeys()) {
                if ( key >= x )
                    i++;
                else 
                    break;
            }
            if (currentPage.isLeaf()) {
                return currentPage;
            }
            else
                return searchPage(key, currentPage.getChild(i));
    }
    /**
     * En este método se sobrescribe toString() para poder imprimir en pantalla las
     * caracteristicas del árbol.
     */
    @Override
    public String toString(){
        int numNodos = 0;
        int altura = 0;
        BPlusPage pagBuffer = this.root;
        while(!pagBuffer.isLeaf()) {
            altura++;
            pagBuffer = pagBuffer.getChild(0);
        }
        do {
            numNodos += pagBuffer.getNodes().size();
            pagBuffer = pagBuffer.getNextPage();
        }while(pagBuffer != null);
        return "◆◆◆◆◆◆◆◆◆ Datos Arbol ◆◆◆◆◆◆◆◆◆\nParametro B:" + B + "\nMinimo keys: " 
                + (B-1) + "\nMaximo keys: " + (2*B-1) + "\nAltura: " + altura + "\nNúmero de nodos: "
                + numNodos + "\n";
    }

    /**
     * Este método se encarga de mostrar la estructura de árbol B+.
     */
    public void mostrarArbol(){
        System.out.println("◆◆◆◆◆◆◆◆◆◆◆ Árbol ◆◆◆◆◆◆◆◆◆◆◆");
        if(this.root.getChildren().isEmpty()==true && this.root.getKeys().isEmpty()==true){
            System.out.println("No hay elementos aun");
            System.out.println(toString());
            return ;
        }
        Queue<BPlusPage> paginas = new LinkedList<>();
        paginas.add(this.root);
        BPlusPage padre=null;
        while( !paginas.isEmpty() ){
            
            BPlusPage v = paginas.poll();
            if(v.getFather()==null){
                System.out.print("Nodo root: ");
            }
            if(padre!=v.getFather()){
                System.out.print("\n\n\nNodo Padre: ");
                v.getFather().showKeys();
                padre=v.getFather();
                System.out.print("\n\t\tNodos:");
            }
            System.out.print("\n\t\t");
            v.showKeys();

            paginas.addAll(v.getChildren());
            /*
            for( int i = 0 ; i < v.getHijos().size() ; i ++ )
                paginas.add( v.getHijos().get(i) );*/
        }
        System.out.println("\n");
        System.out.println(toString());
    }
}