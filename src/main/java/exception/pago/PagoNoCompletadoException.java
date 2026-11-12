/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.pago;

/**
 *
 * @author Sandy
 */
public class PagoNoCompletadoException extends RuntimeException {

    public PagoNoCompletadoException(String mensaje) {
        super(mensaje);
    }
}
