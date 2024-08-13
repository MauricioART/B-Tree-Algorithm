
package proyecto2edaii.arbolbmas;

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
public class ArbolBMas {
    
    private Pagina raiz;
    private int B;

    /**
     * Construye una instancia de clase estableciendo el parametro B.
     * @param B Parámetro de mínimo numero de hijos en pagina intermedia.
     */
    public ArbolBMas(int B) {
        this.raiz = new Pagina();
        this.B = B;
    }
    /**
     * Este método agrega un nodo al árbol con el parámetro clave.
     * @param clave Sirve para ordenar un nuevo nodo al árbol. 
     * @return True si se logro realizar la inserción y false en caso contrario
     */
    public boolean insertarNodo(int clave){
        Pagina pagInsercion = buscarPagina(clave);
        if (!buscarExistencia(clave)) {
            if (pagInsercion.getClaves().size() < 2*B-1) {
                int i = 0;
                for ( int x : pagInsercion.getClaves()) {
                    if (clave > x)
                        i++;
                    else
                        break;
                }
                pagInsercion.getClaves().add(i, clave);
                
                Scanner sc = new Scanner(System.in);
                System.out.print("Ingresa nombre: ");
                String dato1 = sc.nextLine();
                Nodo nuevoNodo = new Nodo(dato1);
                pagInsercion.getNodos().add(i, nuevoNodo);
                return true;
            }
            else {
                return divisionCelular(clave, pagInsercion);
                
            }
        }
        else
            return false;
    }
    /**
     * Método auxiliar de insertarNodo. Se encarga de crear una nueva pagina si la pagina actual está por encima
     * del tope de su capacidad y reparte las claves y/o nodos entre ambas paginas.
     * @param clave Parámetro con el valor a insertar en la pagina.
     * @param pagActual Pagina actual donde se quiere insertar clave
     */
    public boolean divisionCelular(int clave, Pagina pagActual) {
        Pagina nuevaPagina = new Pagina();
        int i = 0;
        for (int x : pagActual.getClaves()) {
            if (clave > x)
                i++;
            else
                break;
        }
        if (pagActual.esHoja()) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Ingresa nombre: ");
            String dato1 = sc.nextLine();
            Nodo nuevoNodo = new Nodo(dato1);
            pagActual.getNodos().add(i, nuevoNodo);
            pagActual.getClaves().add(i, clave);
            int numNodos = pagActual.getNodos().size();
            int numClaves = pagActual.getClaves().size();
            ArrayList<Nodo> SubListaIzqN = new ArrayList(pagActual.getNodos().subList(0, numNodos/2));
            ArrayList<Nodo> SubListaDerN = new ArrayList(pagActual.getNodos().subList(numNodos/2, numNodos));
            ArrayList<Integer> SubListaIzqC = new ArrayList(pagActual.getClaves().subList(0, numClaves/2));
            ArrayList<Integer> SubListaDerC = new ArrayList(pagActual.getClaves().subList(numClaves/2, numClaves));
            pagActual.setNodo(SubListaIzqN);
            nuevaPagina.setNodo(SubListaDerN);
            pagActual.setClave(SubListaIzqC);
            nuevaPagina.setClave(SubListaDerC);
            nuevaPagina.setSigPagina(pagActual.getSigPagina());
            pagActual.setSigPagina(nuevaPagina);
        }
        else {
            nuevaPagina.setHoja(false);
            pagActual.getClaves().add(i, clave);
            int numClaves = pagActual.getClaves().size();
            ArrayList<Integer> SubListaIzq = new ArrayList(pagActual.getClaves().subList(0, numClaves/2));
            ArrayList<Integer> SubListaDer = new ArrayList(pagActual.getClaves().subList(numClaves/2, numClaves));
            pagActual.setClave(SubListaIzq);
            nuevaPagina.setClave(SubListaDer);
            int numHijos = numClaves + 1;
            for (int j = 0; j < B; j++)
                nuevaPagina.getHijos().add(0,pagActual.getHijos().remove(--numHijos));
            for (Pagina x : nuevaPagina.getHijos()) {
                x.setPadre(nuevaPagina);
            }
        }
        
        if (pagActual == this.raiz) {
            Pagina nuevaPagRaiz = new Pagina();
            this.raiz = nuevaPagRaiz;
            this.raiz.setHoja(false);
            pagActual.setPadre(nuevaPagRaiz);
            nuevaPagina.setPadre(nuevaPagRaiz);
            nuevaPagRaiz.getHijos().add(pagActual);
            nuevaPagRaiz.getHijos().add(nuevaPagina);
            if (pagActual.esHoja())
                nuevaPagRaiz.getClaves().add(nuevaPagina.getClave(0));
            else
                nuevaPagRaiz.getClaves().add(nuevaPagina.getClaves().remove(0));
            return true;
        }
        else {
            int indicePagActual = pagActual.getIndiceHijo();
            nuevaPagina.setPadre(pagActual.getPadre());
            pagActual.getPadre().getHijos().add(indicePagActual + 1, nuevaPagina);
            if (pagActual.getPadre().getClaves().size() < 2*B-1) {
                if (pagActual.esHoja())
                    pagActual.getPadre().getClaves().add(indicePagActual, nuevaPagina.getClave(0));
                else
                    pagActual.getPadre().getClaves().add(indicePagActual, nuevaPagina.getClaves().remove(0));
                return true;
            }
            else {
                if (pagActual.esHoja())
                    return divisionCelular(nuevaPagina.getClave(0), pagActual.getPadre());
                else
                    return divisionCelular(nuevaPagina.getClaves().remove(0), pagActual.getPadre());
            }
        }
    }
    /**
     * Este método elimina el nodo que está guardado en el árbol con el valor clave
     * @param clave Valor con el que está guardado el nodo a eliminar.
     * @return True si clave se encuentra en el arbol y false en caso contrario 
     */
    public boolean eliminarNodo(int clave){
        if (buscarExistencia(clave)) {
            Pagina pagActual = buscarPagina(clave);
            int i = 0;
            for (int x : pagActual.getClaves()) {
                if (clave > x)
                    i++;
                else 
                    break;
            }
            pagActual.getClaves().remove(i);
            pagActual.getNodos().remove(i);
            if (pagActual.getClaves().size() >= B-1) {    
                return true;
            }
            else {
                if (pagActual == this.raiz) 
                    return true;
                else {
                    int indicePagActual = pagActual.getIndiceHijo();
                    int prestador = buscarPrestador(indicePagActual, pagActual.getPadre());
                    if ( prestador != 0) {
                        return prestarClave(pagActual, prestador);
                    }
                    else {
                        return unirPaginas(pagActual);
                    }
                }
            }   
        }
        else
            return false;
    }
    /**
     * Método auxiliar de eliminarNodo. Se encarga de realizar las rotaciones de claves y/o nodos
     * cuando una pagina se encuentra con un menor número de claves y/o nodos y existe algúna
     * pagina vecina con suficientes para prestar.
     * @param pagActual Representa la pagina con un deficit de claves.
     * @param prestador Valor que representa que pagina vecina va a prestar. Si se trata del vecino
     * derecho su valor será de 1 y si es el izquierdo sera -1.
     */
    public boolean prestarClave(Pagina pagActual, int prestador) {
        int h = pagActual.getIndiceHijo();
        Pagina pagPrestadora = pagActual.getPadre().getHijo(h + prestador);
        if (prestador == 1) {
            if (pagActual.esHoja()) {
                pagActual.getClaves().add(pagPrestadora.getClaves().remove(0));
                pagActual.getNodos().add(pagPrestadora.getNodos().remove(0));
                pagActual.getPadre().getClaves().set(h, pagPrestadora.getClave(0));
            }
            else {
                pagActual.getClaves().add(pagActual.getPadre().getClave(h));
                pagActual.getPadre().getClaves().set(h, pagPrestadora.getClaves().remove(0));
                pagActual.getHijos().add(pagPrestadora.getHijos().remove(0));
            }
        }
        else {
            int numClavesPrestador = pagPrestadora.getClaves().size();
            if (pagActual.esHoja()) {
                pagActual.getClaves().add(0, pagPrestadora.getClaves().remove(numClavesPrestador-1));
                pagActual.getNodos().add(0, pagPrestadora.getNodos().remove(numClavesPrestador-1));
                pagActual.getPadre().getClaves().set(h-1, pagActual.getClave(0));
            }
            else {
                pagActual.getClaves().add(pagActual.getPadre().getClave(h-1));
                pagActual.getPadre().getClaves().set(h-1, pagPrestadora.getClaves().remove(numClavesPrestador-1));
                pagActual.getHijos().add(0, pagPrestadora.getHijos().remove(numClavesPrestador));
                pagActual.getHijo(0).setPadre(pagActual);
            }
        }
        return true;
    }
    /**
     * Método auxiliar de eliminarNodo. Se encarga de unir paginas vecinas debido a un deficit de claves 
     * en pagActual y no hay nodos vecinos capacez de prestar claves.
     * @param pagActual Pagina con deficit de claves.
     * @return Regresa true si se logra unir las paginas.
     */
    public boolean unirPaginas(Pagina pagActual) {
        int h = pagActual.getIndiceHijo();
        if ( h == pagActual.getPadre().getHijos().size()-1){
            pagActual = pagActual.getPadre().getHijo(h-1);
            h--;
        }
        Pagina pagSiguiente = pagActual.getPadre().getHijo(h+1);
        if (pagActual.esHoja()) {
            for ( Nodo x : pagSiguiente.getNodos()) {
                pagActual.getNodos().add(x);
            }
            for (int x : pagSiguiente.getClaves()) {
                pagActual.getClaves().add(x);
            }
            pagActual.setSigPagina(pagSiguiente.getSigPagina());
        }
        else {
            for (Pagina x : pagSiguiente.getHijos()) {
                x.setPadre(pagActual);
                pagActual.getHijos().add(x);
            }
            pagActual.getClaves().add(pagActual.getPadre().getClave(h));
            for (int x : pagSiguiente.getClaves()) {
                pagActual.getClaves().add(x);
            }
            if (pagActual.getPadre() == this.raiz && this.raiz.getClaves().size() == 0) {
                this.raiz = pagActual;
                this.raiz.setPadre(null);
                pagActual = pagActual.getHijo(0);
            }
        }
        pagActual.getPadre().getClaves().remove(h);
        pagActual.getPadre().getHijos().remove(h+1);
        if (pagActual.getPadre() == this.raiz && pagActual.getPadre().getClaves().isEmpty()) {
            this.raiz = pagActual;
            this.raiz.setPadre(null);
            return true;
        }
        else {
            if (pagActual.getPadre().getClaves().size() < B-1) {
                int prestador = buscarPrestador(pagActual.getPadre().getIndiceHijo(),pagActual.getPadre().getPadre());
                if (prestador != 0) {
                    return prestarClave(pagActual.getPadre(),prestador);
                }
                else {
                    return unirPaginas(pagActual.getPadre());
                }
            }
            else {
                return true;
            }
        }
    }
    /**
     * Este método se encarga de buscar una pagina vecina capaz de prestar claves.
     * @param indiceHijo Indice que tiene la pagina con deficit de claves en la lista de hijos
     * de su pagina padre.
     * @param padre Pagina padre de la pagina con deficit de claves.
     * @return Regresa -1 si la pagina encontrada es el vecino izquierdo, 1 si es el derecho y 0 
     * si no hay ninguna capaz de prestar.
     */
    public int buscarPrestador(int indiceHijo, Pagina padre){
        if(indiceHijo > 0 && indiceHijo < padre.getHijos().size()-1){
            int numClavesIzq = padre.getHijo(indiceHijo - 1).getClaves().size();
            int numClavesDer = padre.getHijo(indiceHijo + 1).getClaves().size();
            if (numClavesIzq >= numClavesDer){
                if(numClavesIzq > B-1)
                    return - 1;
            }else{
                if(numClavesDer > B-1)
                    return 1;
            }
            return 0;
        }
        if(indiceHijo == 0 && padre.getHijo(indiceHijo + 1).getClaves().size() > B-1)
            return 1;
        if(indiceHijo == padre.getHijos().size()-1 && padre.getHijo(indiceHijo - 1).getClaves().size() > B-1)
            return -1;
        return 0;        
    }
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de clave.
     * @param clave Valor relacionado del nodo a buscar.
     * @return True si se encuntra en la estructura y false en caso contrario.
     */
    public boolean buscarExistencia(int clave){
        return buscarExistencia(clave, this.raiz);
    }
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de clave, usando recursividad.
     * @param clave Valor relacionado del nodo a buscar.
     * @param pagActual Representa la pagina en la que busca la clave en cierta llamada recursiva.
     * @return True si el clave se encontro y false en caso contrario.
     */
    public boolean buscarExistencia(int clave, Pagina pagActual){
            int i = 0;
            for (int x : pagActual.getClaves()) {
                if ( clave >= x )
                    i++;
                else 
                    break;
            }
            if (pagActual.esHoja()) {
                for (int x : pagActual.getClaves()) {
                    if (clave == x)
                        return true;
                }
                return false;
            }
            else
                return buscarExistencia(clave, pagActual.getHijo(i));
    }
    
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de clave.
     * @param clave Valor relacionado del nodo a buscar.
     * @return 
     */
    public Nodo buscarNodo(int clave){
        return buscarNodo(clave, this.raiz);
    }
    
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de clave, usando recursividad.
     * @param clave Valor relacionado del nodo a buscar.
     * @param pagActual Representa la pagina en la que busca la clave en cierta llamada recursiva.
     * @return 
     */
    public Nodo buscarNodo(int clave, Pagina pagActual){
        int i = 0;
        for (int x : pagActual.getClaves()) {
            if ( clave >= x )
                i++;
            else 
                break;
        }
        if (pagActual.esHoja()) {
            if (pagActual.getClaves().isEmpty())
                return null;
            if (pagActual.getClave(i-1) == clave)
                return pagActual.getNodo(i-1);
            else
                return null;
        }
        else
            return buscarNodo(clave, pagActual.getHijo(i));
     
    }
    /**
     * Este método utiliza el método homonimo para buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de clave.
     * @param clave Valor relacionado del nodo a buscar.
     * @return Regresa la pagina en donde se encuntra el nodo.
     */
    public Pagina buscarPagina(int clave){
        return buscarPagina(clave, this.raiz);
    }
    /**
     * Este método se encarga de buscar en la estructura si se encuentra algún nodo guardado con 
     * el valor de clave, usando recursividad.
     * @param clave Valor relacionado del nodo a buscar.
     * @param pagActual Representa la pagina en la que busca la clave en cierta llamada recursiva.
     * @return Regresa la pagina en donde se encuntra el nodo.
     */
    public Pagina buscarPagina(int clave, Pagina pagActual){
            int i = 0;
            for (int x : pagActual.getClaves()) {
                if ( clave >= x )
                    i++;
                else 
                    break;
            }
            if (pagActual.esHoja()) {
                return pagActual;
            }
            else
                return buscarPagina(clave, pagActual.getHijo(i));
    }
    /**
     * En este método se sobrescribe toString() para poder imprimir en pantalla las
     * caracteristicas del árbol.
     */
    @Override
    public String toString(){
        int numNodos = 0;
        int altura = 0;
        Pagina pagBuffer = this.raiz;
        while(!pagBuffer.esHoja()) {
            altura++;
            pagBuffer = pagBuffer.getHijo(0);
        }
        do {
            numNodos += pagBuffer.getNodos().size();
            pagBuffer = pagBuffer.getSigPagina();
        }while(pagBuffer != null);
        return "◆◆◆◆◆◆◆◆◆ Datos Arbol ◆◆◆◆◆◆◆◆◆\nParametro B:" + B + "\nMinimo claves: " 
                + (B-1) + "\nMaximo claves: " + (2*B-1) + "\nAltura: " + altura + "\nNúmero de nodos: "
                + numNodos + "\n";
    }
    /**
     * Este método se encraga de mostrar la estructura de árbol B+.
     */
    public void mostrarArbol(){
        System.out.println("◆◆◆◆◆◆◆◆◆◆◆ Árbol ◆◆◆◆◆◆◆◆◆◆◆");
        if(this.raiz.getHijos().isEmpty()==true && this.raiz.getClaves().isEmpty()==true){
            System.out.println("No hay elementos aun");
            System.out.println(toString());
            return ;
        }
        Queue<Pagina> paginas = new LinkedList<>();
        paginas.add(this.raiz);
        Pagina padre=null;
        while( !paginas.isEmpty() ){
            
            Pagina v = paginas.poll();
            if(v.getPadre()==null){
                System.out.print("Nodo Raiz: ");
            }
            if(padre!=v.getPadre()){
                System.out.print("\n\n\nNodo Padre: ");
                v.getPadre().mostrarLlaves();
                padre=v.getPadre();
                System.out.print("\n\t\tNodos:");
            }
            System.out.print("\n\t\t");
            v.mostrarLlaves();
            
            for( int i = 0 ; i < v.getHijos().size() ; i ++ )
                paginas.add( v.getHijos().get(i) );
        }
        System.out.println("\n");
        System.out.println(toString());
        
    }
}
