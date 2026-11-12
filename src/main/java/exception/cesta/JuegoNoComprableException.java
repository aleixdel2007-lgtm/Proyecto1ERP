/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.cesta;

/**
 *
 * @author Sandy
 */
public class JuegoNoComprableException extends RuntimeException {

    // El mensaje es la clave de traduccion (cesta.error.*): el controlador la resuelve segun el idioma
    public JuegoNoComprableException(String mensaje) {
        super(mensaje);

    }

}
