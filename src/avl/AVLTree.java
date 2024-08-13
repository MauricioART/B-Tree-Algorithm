/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto2edaii.avl;

/**
 *
 * @author DELL-PC
 */
public class AVLTree {
    public Node root; 
  
    public int alturaTree(Node n) { //Obtenemos la altura del árbol
        if (n == null) 
            return 0; 
        return n.altura; 
    } 
  
        /* Método principal para la inserción de un valor en el árbol
        Los parámetros que recibe es un nodo (el nodo raíz del árbol) y el valor a insertar   
    */
    public Node add(Node node, int valor) {  //Se usa el álgoritmo BFS para visitar todos los nodos
  
        if (node == null) { //Si la raíz es null, ahí insertamos el nuevo nodo 
            System.out.println("Se inserto el "+valor+" de manera correcta");
            return (new Node(valor)); 
        }
        if (valor < node.valor) { //Varificamos si el valor a insertar es menor a la raíz 
            node.izq = add(node.izq, valor); 
        }
        else if (valor > node.valor) { //Verificamos si el valor a insertar es mayor
            node.der = add(node.der, valor); 
        }
        else if (valor==node.valor) { //Verificamos que no sean iguales las claves
            System.out.println("No se permiten llaves duplicadas");
            return node;  //Retornamos el nodo sin insertar nada 
        }
        
        //Actualización de la altura del nodo
        node.altura = 1 + numMayor(alturaTree(node.izq), alturaTree(node.der)); 
  
        /* Tenemos que verificar si árbol sigue balanceado después de la inserción, para esto
           obtenemos primero el factor de balance */
        int balance = getBalance(node); 
  
        // Si el árbol no está valanciado, ha que ver en donde se encuentra el desvalanceo y corregirlo
        if (balance > 1 && valor < node.izq.valor) 
            return rotacionDer(node); 
  
        if (balance < -1 && valor > node.der.valor) 
            return rotacionIzq(node); 
  
        if (balance > 1 && valor > node.izq.valor) { 
            node.izq = rotacionIzq(node.izq); 
            return rotacionDer(node); 
        } 
  
        if (balance < -1 && valor < node.der.valor) { 
            node.der = rotacionDer(node.der); 
            return rotacionIzq(node); 
        } 
  
        //Si no hubo ningún desvalancio en el árbo, retornamos el nodo tal cual
        return node; 
    } 
   
    //Para la eliminación necesiramos el nodo raíz del árbol y el valor que se quiere eliminar
    public Node eliminar(Node root, int valor) {  
       if(busqueda(valor,root)) {
            //El primer caso es si el el árbol está vacio
            if (root == null) { 
                return root;  
            }
           
             /* Si la clave a eliminar es más pequeña que la clave de la raíz, 
             entonces se encuentra en el subárbol izquierdo*/
            if (valor < root.valor){
                root.izq = eliminar(root.izq, valor);  
            }
             /* Si la clave que se va a eliminar es mayor que la clave de la raíz, 
             entonces se encuentra en el subárbol derecho */
            else if (valor > root.valor) {
                root.der = eliminar(root.der, valor);
            }
             /*Si la clave a eliminar no es ni mayor ni menor que la raíz, signidica que se
             puede encontrar en la raíz*/
            else {  
                Node aux;
                if ((root.izq == null) || (root.der == null)) { //Verificamos no tiene hijo o almenos uno  
                    aux = null; //Necesitamos un nodo auxiliar para verificar a existencia de los hijos 
                    if (aux == root.izq) {
                        aux = root.der;  
                    }
                    else {
                        aux = root.izq;  
                    }
                    if (aux == null) {  //En el caso de que no tenga hijos
                        aux = root;  
                        root = null; //Eliminamos la raíz ya que aquí es donde se encuentra el valor a eliminar 
                    }  
                    else { // En el caso de que solo tenga un hijo 
                        root = aux; 
                    } 
                }  
                else {  //Si tiene ambos hijos 
                    aux = nodoMenor(root.der);  //Obtenemos el hijo menor 
                    root.valor = aux.valor;  //Cambiamos el valor de root 
                    root.der = eliminar(root.der, aux.valor);  //eliminamos el nodo menor 
                }  
            } 
            System.out.println("Se ha eliminado la clave"); 
            if (root == null){  //Verificamos de nuevo si el árbol está vacio depsues de la eliminación
                return root;  
            }
             //Una vez eliminado el nodo, hay que actualizar la áltura del árbol
            root.altura = numMayor(alturaTree(root.izq), alturaTree(root.der)) + 1;  
             //Obtenermos el factor de balance para verificar si en árbol está balanciado o no 
                int balance = getBalance(root);  

            // Si el árbol no está valanciado, ha que ver en donde se encuentra el desvalanceo y corregirlo 
             if (balance > 1 && getBalance(root.izq) >= 0) {
                 return rotacionDer(root);  
             }
             if (balance > 1 && getBalance(root.izq) < 0) {  
                 root.izq = rotacionIzq(root.izq);  
                 return rotacionDer(root);  
             }  
             if (balance < -1 && getBalance(root.der) <= 0) {
                 return rotacionIzq(root);  
             }
             if (balance < -1 && getBalance(root.der) > 0) {  
                 root.der = rotacionDer(root.der);  
                 return rotacionIzq(root);  
             }  

             return root;  
             }
        else {
            System.out.println("El valor no se cuentra en el árbol");
            return root;
        }
    }  
     public boolean busqueda(int valor, Node root){
        //manda a llamar al método busqueda1 enviando como argumentos el valor a buscar y el nodo raíz
     return(busqueda2(valor,root));
    }    
    
    private boolean busqueda2(int valor, Node root){ 
        //Si la raíz es nulla, no se encuentra el valor buscado
        if(root==null){            
            return false;
        // si el valor es iigual al valor del nodo actual, regresa true
        }else if(root.valor==valor){           
            return true;
        //Si el valor buscado es menor al valor del nodo actual, se busca en el suarbol izquierdo
        }else if(root.valor>valor){           
            return busqueda(valor, root.izq); //Recursividad 
        //Si el valor buscado es mayor al valor del nodo actual, se busca en el suarbol derecho
        }else if(root.valor<valor){         
            return busqueda(valor,root.der);
        }   
        return false;
    }
    //Métodos similares a los de busqueda y busqueda1, con la diferencia de que estos devueven el nodo buscado
    public Node busquedaN(int valor, Node root){
        return(busqueda3(valor,root));
    }   
    
    private Node busqueda3(int valor, Node root){
        if(root==null){  
            return null;            
        }
        else if(root.valor==valor){
            return root;
        }
        else if(root.valor>valor){
            return busqueda3(valor, root.izq);
        }
        else if(root.valor<valor){
            return busqueda3(valor,root.der);
        }   
        return null;
    }
     private int numMayor(int num1, int num2) {  //Se obtiene el mayor de dos números
        if(num1>num2)
            return num1;
        else
            return num2;
    } 
  
 
    private Node rotacionDer(Node rootActual) {  //Rotación a la derecha
        Node nuevoRoot = rootActual.izq; 
        Node T2 = nuevoRoot.der; 
  
        nuevoRoot.der = rootActual; //Se realiza la rotación
        rootActual.izq = T2; 
  
        // Como se realizó la rotación, las alturas cambia.
        //Aqui actualizamos las alturas
        rootActual.altura = numMayor(alturaTree(rootActual.izq), alturaTree(rootActual.der)) + 1; 
        nuevoRoot.altura = numMayor(alturaTree(nuevoRoot.izq), alturaTree(nuevoRoot.der)) + 1; 
  
        return nuevoRoot; //Retornamos la nueva raiz una vez terminada la rotación
    } 
  
     
    private Node rotacionIzq(Node rootActual) { //Rotación a la izquierda 
        Node nuevoRoot = rootActual.der; 
        Node T2 = nuevoRoot.izq; 
  
        nuevoRoot.izq = rootActual; 
        rootActual.der = T2; 
  
        rootActual.altura = numMayor(alturaTree(rootActual.izq), alturaTree(rootActual.der)) + 1; 
        nuevoRoot.altura = numMayor(alturaTree(nuevoRoot.izq), alturaTree(nuevoRoot.der)) + 1; 
  
        return nuevoRoot; //Retornamos la nueva raiz una vez terminada la rotación
    } 
  
  
    private int getBalance(Node n) { //Establecemos el factor de balanceo
        if (n == null) 
            return 0; 
  
        return alturaTree(n.izq) - alturaTree(n.der); 
    } 
    
    
    //Este método nos devuelve el valor mínimo que se encuentra en el árbol
    private Node nodoMenor(Node node)  {  
        Node aux = node;  
        //Sabemos que en el ALV los valores que se encuentran a la izquierda del nodo, son menores
        while (aux.izq != null){  
            aux = aux.izq;  
        }
        return aux;  
    }  
    
  
    // Para imprirmir el árbol utilizamos el recorrido en pre orden
    public void preOrder(Node node) { 
        if (node != null) { 
            System.out.print(node.valor + " "); 
            preOrder(node.izq); 
            preOrder(node.der); 
        } 
    } 
}
