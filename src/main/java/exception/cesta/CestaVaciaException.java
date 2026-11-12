/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.cesta;

/**
 *
 * @author Sandy
 */
public class CestaVaciaException extends RuntimeException {

    public CestaVaciaException() {
        // El mensaje es la clave de traduccion: el controlador la resuelve segun el idioma
        super("cesta.error.vacia");
    }
}