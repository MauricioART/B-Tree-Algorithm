/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto2edaii;

import java.util.InputMismatchException;
import java.util.Scanner;
import proyecto2edaii.arbolbmas.ArbolBMas;
import proyecto2edaii.arbolbmas.Nodo;
import proyecto2edaii.avl.AVLTree;

/**
 * Este método contiene la interfaz de usuario para manejar las estructuras de datos Árbol AVL y
 * Árbol B+.
 * @author macosx
 */
public class Control {
    
    public static void menuPrincipal() {
        boolean seguir = true;
        try {
        do {
            Scanner sc = new Scanner(System.in);
            Scanner sc2 = new Scanner(System.in);
            System.out.println("\n◆◆◆◆◆◆◆◆◆◆ Equipo 5 ◆◆◆◆◆◆◆◆◆◆");
            System.out.println("      ◆◆◆ Proyeto #2 ◆◆◆");
            System.out.println("1.-Árbol AVL");
            System.out.println("2.-Árbol B+");
            System.out.println("3.-Salir");
            System.out.print("Opción: ");
            switch(sc.nextInt()) {
                case 1:
                    menuArbolAVL();
                    break;
                case 2:
                    System.out.print("Ingresa parámetro B: ");
                    int B = sc2.nextInt();
                    if (B >= 2)
                        menuArbolBMas(new ArbolBMas(B));
                    else
                        System.out.println(">>>>>Valor invalido");
                    break;
                case 3:
                    System.out.println("\nFin del programa, bye");
                    seguir = false;
                    break;
                default:
                    System.out.println(">>>>>Opción invalida");
            
            }
            
        }while(seguir);
        }catch(InputMismatchException e) {
            System.out.println("Solo se aceptan números");
            menuPrincipal();
        }
    }
    
    /**
     * Método con menu de Árbol AVL
     */
    public static void menuArbolAVL() {
        AVLTree arbol = new AVLTree(); 
  
        Scanner sc= new Scanner(System.in);
        int num;
        int op=0;
        try {
            do {
                System.out.println("\n◆◆◆◆◆◆◆◆◆◆ Árbol AVL ◆◆◆◆◆◆◆◆◆◆");
                System.out.println("1. Inserción");
                System.out.println("2. Eliminación");
                System.out.println("3. Busqueda");
                System.out.println("4. Imprimir árbol");
                System.out.println("5. Datos del árbol");
                System.out.println("6. Salir");
                System.out.print("R: ");
                op=sc.nextInt();

                switch (op) {
                    case 1:
                        System.out.println("\nINSERCIÓN");
                        System.out.print("Dame el número a insertar: ");
                        num=sc.nextInt();
                        arbol.root = arbol.add(arbol.root, num); 
                    break;
                    case 2:
                        System.out.println("\nELIMINICACIÓN");
                        System.out.print("Dame el número a eliminar: ");
                        num=sc.nextInt();
                        arbol.root=arbol.eliminar(arbol.root, num);
                    break;
                    case 3:
                        System.out.println("\nBÚSQUEDA");
                        System.out.print("Dame el número a buscar: ");
                        num=sc.nextInt();
                        System.out.println("¿El valor se encuentra en el árbol? R: "+arbol.busqueda( num,arbol.root));
                        if (arbol.busqueda( num,arbol.root)) {
                            System.out.println("Se encuntra en la altura: "+(arbol.busquedaN(num, arbol.root).altura-1));
                        }
                    break;
                    case 4:
                        System.out.println("\nÁRBOL");
                        System.out.println("El recorrido en preorden es: "); 
                        arbol.preOrder(arbol.root); 
                    break;
                    case 5:
                        System.out.println("\nDATOS DEL ÁRBOL");
                        System.out.println("Altura: "+(arbol.alturaTree(arbol.root)-1));
                        System.out.println("Raíz: "+arbol.root.valor);
                        
                    break;
                    default:
                        System.out.println("\nOpción incorrecta");
                    break;
                }
            }while(op!=6);
        }catch (InputMismatchException e) {
            System.out.println("Solo se aceptan números");
            menuArbolAVL();
        }catch (NullPointerException e) {
            System.out.println("Árbol vacio");
            menuArbolAVL();
        } 
    
    }
    /**
     * Método con menu de Árbol B+.
     * @param arbol arbolBMas
     */
    public static void menuArbolBMas(ArbolBMas arbol) {
        boolean seguir = true;
        do {
            try {
                Scanner sc = new Scanner(System.in);
                Scanner sc2 = new Scanner(System.in);
                System.out.println("◆◆◆◆◆◆◆◆◆◆ Árbol B+ ◆◆◆◆◆◆◆◆◆◆");
                System.out.println("1.-Insertar nodo");
                System.out.println("2.-Eliminar nodo");
                System.out.println("3.-Buscar nodo");
                System.out.println("4.-Mostrar Arbol");
                System.out.println("5.-Salir");
                System.out.print("Opción: ");
                switch(sc.nextInt()) {
                    case 1:
                        System.out.print("Clave: ");
                        if(arbol.insertarNodo(sc2.nextInt()))
                            System.out.println("◆◆◆◆◆ Inserción exitosa ◆◆◆◆◆");
                        else
                            System.out.println("◆◆◆◆◆◆◆ Clave repetida ◆◆◆◆◆◆◆");
                        break;
                    case 2:
                        System.out.print("Clave: ");
                        if (arbol.eliminarNodo(sc2.nextInt()))
                            System.out.println("◆◆◆◆◆ Eliminación exitosa ◆◆◆◆◆");
                        else
                            System.out.println("◆◆◆◆◆ Clave no encontrada ◆◆◆◆◆");
                        break;
                    case 3:
                        System.out.print("Clave: ");
                        Nodo nodoEncontrado;
                        if ((nodoEncontrado = arbol.buscarNodo(sc2.nextInt())) != null)
                            System.out.println(nodoEncontrado);
                        else
                            System.out.println("◆◆◆◆◆ Clave no encontrada ◆◆◆◆◆");
                        break;
                    case 4:
                        arbol.mostrarArbol();
                        break;
                    case 5:
                        seguir = false;
                        break;
                    default:
                        System.out.println("◆◆◆◆◆◆ Opción invalida ◆◆◆◆◆◆");
                }
            } catch (InputMismatchException e) {
                System.out.println("◆◆◆◆◆ Clave no númerica ◆◆◆◆◆");
                menuArbolBMas(arbol);
            }
        }while(seguir);
    }
    
}
