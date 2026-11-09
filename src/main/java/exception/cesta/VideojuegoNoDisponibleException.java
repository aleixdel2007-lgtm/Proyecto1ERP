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
        super("Este videojuego no esta disponible");
    }
}
