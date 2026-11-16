/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.pago;

/**
 * Se lanza cuando un juego de la cesta se ha quedado sin claves disponibles.
 *
 * Como el resto de excepciones, el mensaje es la clave de traduccion y no el texto. El
 * titulo del juego viaja aparte porque es el parametro {0} de esa clave: el controlador
 * lo saca con getTitulo() y se lo pasa al MessageSource.
 *
 * @author Sandy
 */
public class SinClavesDisponiblesException extends RuntimeException {

    private final String titulo;

    public SinClavesDisponiblesException(String titulo) {
        super("pago.error.sinClaves");
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
