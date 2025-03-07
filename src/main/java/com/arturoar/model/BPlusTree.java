package com.arturoar.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

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
        this.B = B > MAX_B_SIZE ? MAX_B_SIZE : B;
    }

    /**
     * Este método agrega un nodo al árbol con el parámetro key.
     * @param key Sirve para ordenar un nuevo nodo al árbol. 
     * @return True si se logro realizar la inserción y false en caso contrario
     */
    public boolean insertNode(int key, String data) {
        BPlusPage pagInsercion = searchPage(key);
        BPlusTraversalResult<Boolean> containResult = this.contains(key);
        BPlusTraversalResult<BPlusNode> insertResult = new BPlusTraversalResult<>(containResult.getVisitedNodes());
        ArrayList<BPlusNode> createdNodes = new ArrayList<>();
        if (!containResult.getResult()) {
            if (pagInsercion.getKeys().size() < 2*B-1) {
                int i = 0;
                for ( BPlusNode node : pagInsercion.getKeys()) {
                    if (key > node.getKey())
                        i++;
                    else
                        break;
                }
                pagInsercion.getKeys().add(i, new BPlusNode(key));
                
                BPlusLeafNode newNode = new BPlusLeafNode(key,data);
                insertResult.setResult(newNode);
                pagInsercion.getNodes().add(i, newNode);
                return true;
            }
            else {
                return celularDivision(key, pagInsercion, data);
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
    private boolean celularDivision(int key, BPlusPage currentPage, String data) {
        BPlusPage newPage = new BPlusPage();
        int i = 0;
        for (BPlusNode node : currentPage.getKeys()) {
            if (key > node.getKey())
                i++;
            else
                break;
        }
        if (currentPage.isLeaf()) {
            BPlusLeafNode newNode = new BPlusLeafNode(key,data);
            currentPage.getNodes().add(i, newNode);
            currentPage.getKeys().add(i, new BPlusNode(key));
            int numNodos = currentPage.getNodes().size();
            int numkeys = currentPage.getKeys().size();
            ArrayList<BPlusLeafNode> SubListaIzqN = new ArrayList(currentPage.getNodes().subList(0, numNodos/2));
            ArrayList<BPlusLeafNode> SubListaDerN = new ArrayList(currentPage.getNodes().subList(numNodos/2, numNodos));
            ArrayList<BPlusNode> SubListaIzqC = new ArrayList(currentPage.getKeys().subList(0, numkeys/2));
            ArrayList<BPlusNode> SubListaDerC = new ArrayList(currentPage.getKeys().subList(numkeys/2, numkeys));
            currentPage.setNodes(SubListaIzqN);
            newPage.setNodes(SubListaDerN);
            currentPage.setKeys(SubListaIzqC);
            newPage.setKeys(SubListaDerC);
            newPage.setNextPage(currentPage.getNextPage());
            currentPage.setNextPage(newPage);
        }
        else {
            newPage.setLeaf(false);
            currentPage.getKeys().add(i,new BPlusNode(key));
            int numOfKeys = currentPage.getKeys().size();
            ArrayList<BPlusNode> SubListaIzq = new ArrayList(currentPage.getKeys().subList(0, numOfKeys/2));
            ArrayList<BPlusNode> SubListaDer = new ArrayList(currentPage.getKeys().subList(numOfKeys/2, numOfKeys));
            currentPage.setKeys(SubListaIzq);
            newPage.setKeys(SubListaDer);
            int numHijos = numOfKeys + 1;
            for (int j = 0; j < B; j++)
                newPage.getChildren().add(0,currentPage.getChildren().remove(--numHijos));
            for (BPlusPage x : newPage.getChildren()) {
                x.setParent(newPage);
            }
        }
        
        if (currentPage == this.root) {
            BPlusPage nuevaPagroot = new BPlusPage();
            this.root = nuevaPagroot;
            this.root.setLeaf(false);
            currentPage.setParent(nuevaPagroot);
            newPage.setParent(nuevaPagroot);
            nuevaPagroot.getChildren().add(currentPage);
            nuevaPagroot.getChildren().add(newPage);
            if (currentPage.isLeaf())
                nuevaPagroot.getKeys().add(new BPlusNode(newPage.getKey(0)));
            else
                nuevaPagroot.getKeys().add(newPage.getKeys().removeFirst());
            return true;
        }
        else {
            int indicePagActual = currentPage.getChildrenIndex();
            newPage.setParent(currentPage.getParent());
            currentPage.getParent().getChildren().add(indicePagActual + 1, newPage);
            if (currentPage.getParent().getKeys().size() < 2*B-1) {
                if (currentPage.isLeaf())
                    currentPage.getParent().getKeys().add(indicePagActual, new BPlusNode(newPage.getKey(0)));
                else
                    currentPage.getParent().getKeys().add(indicePagActual, newPage.getKeys().remove(0));
                return true;
            }
            else {
                if (currentPage.isLeaf())
                    return celularDivision(newPage.getKey(0), currentPage.getParent(), data);
                else
                    return celularDivision(newPage.getKeys().removeFirst().getKey(), currentPage.getParent(), data);
            }
        }
    }
    /**
     * Este método elimina el nodo que está guardado en el árbol con el valor key
     * @param key Valor con el que está guardado el nodo a eliminar.
     * @return True si key se encuentra en el arbol y false en caso contrario 
     */
    public BPlusTraversalResult<ArrayList<BPlusNode>> removeNode(int key){

        BPlusTraversalResult<Boolean> containsResult = this.contains(key);
        BPlusTraversalResult<ArrayList<BPlusNode>> removeResult = new BPlusTraversalResult<>( containsResult.getVisitedNodes());

        if (containsResult.getResult()) {
            BPlusPage currentPage = searchPage(key);
            int i = 0;
            for (BPlusNode node : currentPage.getKeys()) {
                if (key > node.getKey())
                    i++;
                else 
                    break;
            }

            ArrayList<BPlusNode> removedNodes = new ArrayList<>(); 
            removedNodes.add(currentPage.getKeys().remove(i));
            removedNodes.add(currentPage.getNodes().remove(i));
            removeResult.setResult(removedNodes);

            if (currentPage.getKeys().size() >= B-1) {    
                return removeResult;
            }
            else {
                if (currentPage == this.root) 
                    return removeResult;
                else {
                    int indicePagActual = currentPage.getChildrenIndex();
                    int prestador = searchBorrower(indicePagActual, currentPage.getParent());
                    if ( prestador != 0) {

                        borrowKey(currentPage, prestador);
                    }
                    else {
                        mergePages(currentPage);
                    }
                    return removeResult;
                }
            }   
        }
        else
            return removeResult;
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
        BPlusPage pagPrestadora = pagActual.getParent().getChild(h + prestador);
        if (prestador == 1) {
            if (pagActual.isLeaf()) {
                pagActual.getKeys().add(pagPrestadora.getKeys().removeFirst());
                pagActual.getNodes().add(pagPrestadora.getNodes().removeFirst());
                pagActual.getParent().getKeys().set(h, new BPlusNode(pagPrestadora.getKey(0)));
            }
            else {
                pagActual.getKeys().add(new BPlusNode(pagActual.getParent().getKey(h)));
                pagActual.getParent().getKeys().set(h, pagPrestadora.getKeys().removeFirst());
                pagActual.getChildren().add(pagPrestadora.getChildren().removeFirst());
            }
        }
        else {
            int numkeysPrestador = pagPrestadora.getKeys().size();
            if (pagActual.isLeaf()) {
                pagActual.getKeys().add(0, pagPrestadora.getKeys().remove(numkeysPrestador-1));
                pagActual.getNodes().add(0, pagPrestadora.getNodes().remove(numkeysPrestador-1));
                pagActual.getParent().getKeys().set(h-1,new BPlusNode( pagActual.getKey(0)));
            }
            else {
                pagActual.getKeys().add(new BPlusNode(pagActual.getParent().getKey(h-1)));
                pagActual.getParent().getKeys().set(h-1, pagPrestadora.getKeys().remove(numkeysPrestador-1));
                pagActual.getChildren().add(0, pagPrestadora.getChildren().remove(numkeysPrestador));
                pagActual.getChild(0).setParent(pagActual);
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
        if ( h == pagActual.getParent().getChildren().size()-1){
            pagActual = pagActual.getParent().getChild(h-1);
            h--;
        }
        BPlusPage pagSiguiente = pagActual.getParent().getChild(h+1);
        if (pagActual.isLeaf()) {
            for ( BPlusLeafNode x : pagSiguiente.getNodes()) {
                pagActual.getNodes().add(x);
            }
            for (BPlusNode node : pagSiguiente.getKeys()) {
                pagActual.getKeys().add(new BPlusNode(node.getKey()));
            }
            pagActual.setNextPage(pagSiguiente.getNextPage());
        }
        else {
            for (BPlusPage x : pagSiguiente.getChildren()) {
                x.setParent(pagActual);
                pagActual.getChildren().add(x);
            }
            pagActual.getKeys().add(new BPlusNode(pagActual.getParent().getKey(h)));
            for (BPlusNode x : pagSiguiente.getKeys()) {
                pagActual.getKeys().add(x);
            }
            if (pagActual.getParent() == this.root && this.root.getKeys().size() == 0) {
                this.root = pagActual;
                this.root.setParent(null);
                pagActual = pagActual.getChild(0);
            }
        }
        pagActual.getParent().getKeys().remove(h);
        pagActual.getParent().getChildren().remove(h+1);
        if (pagActual.getParent() == this.root && pagActual.getParent().getKeys().isEmpty()) {
            this.root = pagActual;
            this.root.setParent(null);
            return true;
        }
        else {
            if (pagActual.getParent().getKeys().size() < B-1) {
                int prestador = searchBorrower(pagActual.getParent().getChildrenIndex(),pagActual.getParent().getParent());
                if (prestador != 0) {
                    return borrowKey(pagActual.getParent(),prestador);
                }
                else {
                    return mergePages(pagActual.getParent());
                }
            }
            else {
                return true;
            }
        }
    }
    /**
     * Este método se encarga de buscar una pagina vecina capaz de prestar keys.
     * @param childIndex Indice que tiene la pagina con deficit de keys en la lista de hijos
     * de su pagina padre.
     * @param parent Pagina padre de la pagina con deficit de keys.
     * @return Regresa -1 si la pagina encontrada es el vecino izquierdo, 1 si es el derecho y 0 
     * si no hay ninguna capaz de prestar.
     */
    private int searchBorrower(int childIndex, BPlusPage parent){
        if(childIndex > 0 && childIndex < parent.getChildren().size()-1){
            int numkeysIzq = parent.getChild(childIndex - 1).getKeys().size();
            int numkeysDer = parent.getChild(childIndex + 1).getKeys().size();
            if (numkeysIzq >= numkeysDer){
                if(numkeysIzq > B-1)
                    return - 1;
            }else{
                if(numkeysDer > B-1)
                    return 1;
            }
            return 0; 
        }
        if(childIndex == 0 && parent.getChild(childIndex + 1).getKeys().size() > B-1)
            return 1;
        if(childIndex == parent.getChildren().size()-1 && parent.getChild(childIndex - 1).getKeys().size() > B-1)
            return -1;
        return 0;        
    }
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key.
     * @param key Valor relacionado del nodo a buscar.
     * @return True si se encuntra en la estructura y false en caso contrario.
     */
    /**
    public boolean contains(int key){
        return contains(key, this.root);
    }*/
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key, usando recursividad.
     * @param key Valor relacionado del nodo a buscar.
     * @param currentPage Representa la pagina en la que busca la key en cierta llamada recursiva.
     * @return True si el key se encontro y false en caso contrario.
     */
    /* 
    private boolean contains(int key, BPlusPage currentPage){
            int i = 0;
            for (BPlusNode x : currentPage.getKeys()) {
                if ( key >= x.getKey() )
                    i++;
                else 
                    break;
            }
            if (currentPage.isLeaf()) {
                for (BPlusNode x : currentPage.getKeys()) {
                    if (key == x.getKey())
                        return true;
                }
                return false;
            }
            else
                return contains(key, currentPage.getChild(i));
    }*/

    private BPlusTraversalResult<Boolean> contains(int key){
        BPlusTraversalResult<Boolean> result = new BPlusTraversalResult<>(false);
        ArrayDeque<BPlusPage> pageDeque = new ArrayDeque<>();
        pageDeque.add(this.root);
        while(!pageDeque.isEmpty()){
            int i = 0;
            BPlusPage currentPage = pageDeque.pop();
            if (currentPage.isLeaf()){
                for (BPlusLeafNode leafNode : currentPage.getNodes()) {
                    result.addVisitedNode(leafNode);
                    if (key == leafNode.getKey())
                        result.setResult(true);
                }
                return result;
            }else{
                for (BPlusNode intNode: currentPage.getKeys()){
                    result.addVisitedNode(intNode);
                    if (key >= intNode.getKey()){
                        i++;
                    }else{
                        break;
                    }
                }

                pageDeque.add(currentPage.getChild(i));
            }
        }
        
        return result;
    }
    
    public BPlusTraversalResult<BPlusLeafNode> searchKeyIterativo(int key){
        BPlusTraversalResult<BPlusLeafNode> result = new BPlusTraversalResult<>();
        ArrayDeque<BPlusPage> pageDeque = new ArrayDeque<>();
        pageDeque.add(this.root);
        while(!pageDeque.isEmpty()){
            int i = 0;
            BPlusPage currentPage = pageDeque.pop();
            if (currentPage.isLeaf()){
                for (BPlusLeafNode leafNode : currentPage.getNodes()) {
                    result.addVisitedNode(leafNode);
                    if (key == leafNode.getKey()){
                        result.setResult(leafNode);
                        return result;
                    }
                }
            }else{

                for (BPlusNode intNode: currentPage.getKeys()){
                    result.addVisitedNode(intNode);
                    if (key >= intNode.getKey()){
                        i++;
                    }else{
                        break;
                    }
                }

                pageDeque.add(currentPage.getChild(i));
            }
            
        }
        return result;
        
    } 
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key.
     * @param key Valor relacionado del nodo a buscar.
     * @return 
     */
    public BPlusLeafNode searchKey(int key){
        return searchKey(key, this.root);
    }
    
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de key, usando recursividad.
     * @param key Valor relacionado del nodo a buscar.
     * @param currentPage Representa la pagina en la que busca la key en cierta llamada recursiva.
     * @return 
     */
    private BPlusLeafNode searchKey(int key, BPlusPage currentPage){
        int i = 0;
        for (BPlusNode x : currentPage.getKeys()) {
            if ( key >= x.getKey() )
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
            for (BPlusNode x : currentPage.getKeys()) {
                if ( key >= x.getKey() )
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

            paginas.addAll(v.getChildren());
            /*
            for( int i = 0 ; i < v.getHijos().size() ; i ++ )
                paginas.add( v.getHijos().get(i) );*/
        }
        System.out.println("\n");
        System.out.println(toString());
    }
}