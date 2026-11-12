/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.cesta;

/**
 *
 * @author Sandy
 */
public class VideojuegoNoDisponibleException extends RuntimeException {

    public VideojuegoNoDisponibleException() {
        // El mensaje es la clave de traduccion: el controlador la resuelve segun el idioma
        super("cesta.error.noDisponible");
    }
}
