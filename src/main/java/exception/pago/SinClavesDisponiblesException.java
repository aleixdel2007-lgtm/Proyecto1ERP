/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.pago;

/**
 *
 * @author Sandy
 */
public class SinClavesDisponiblesException extends RuntimeException {

    public SinClavesDisponiblesException(String titulo) {
        super("De momento no quedan claves de \"" + titulo
                + "\". Retira ese juego de la cesta o inténtalo más tarde");
    }
}