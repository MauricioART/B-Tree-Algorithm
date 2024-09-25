
package proyecto2edaii.arbolbmas;

/**
 *  Este clase representa la unidad de información a guardar en la estructura.
 * @author Aguilera Roa Mauricio Arturo
 */
public class Nodo {
    
    private String dato1;
    private int dato2;

    /**
     * Construye una instancia de la clase Nodo inicializando a dato1 y dato2
     * @param dato1 String para inicializar dato1
     * @param dato2 Entero para inicializar dato2
     */
    public Nodo(String dato1, int dato2) {
        this.dato1 = dato1;
        this.dato2 = dato2;
    }
    /**
     * Construye una instancia de la clase Nodo inicializando a dato1
     * @param dato1 String para inicializar dato1
     */
    public Nodo(String dato1) {
        this.dato1 = dato1;
        this.dato2 = 0;
    }
    
    public String getDato1() {
        return dato1;
    }
    
    public void setDato1(String dato1) {
        this.dato1 = dato1;
    }
    
    public int getDato2() {
        return dato2;
    }
    
    public void setDato2(int dato2) {
        this.dato2 = dato2;
    }

    @Override
    public String toString() {
        return "◆◆◆◆◆◆◆◆◆ Datos nodo ◆◆◆◆◆◆◆◆◆\nNombre: " + this.dato1 
                + "\nEdad: " + dato2 ;
    }
    
}
