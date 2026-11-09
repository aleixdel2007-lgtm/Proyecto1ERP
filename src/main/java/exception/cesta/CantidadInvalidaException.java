/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.cesta;

/**
 *
 * @author Sandy
 */
public class CantidadInvalidaException extends RuntimeException {

    public CantidadInvalidaException() {
        super("La cantidad tiene que ser al menos 1");
    }
}
