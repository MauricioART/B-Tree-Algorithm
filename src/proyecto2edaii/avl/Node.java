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
public class Node {
    public int valor; 
    public int altura; 
    public Node izq;
    public Node der; 
  
    public Node(int d) { 
        valor = d; 
        altura = 1; 
    } 
}
