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

    private final String titulo;

    // El mensaje es la clave de traduccion (pago.error.sinClaves) y el titulo es su {0}:
    // el controlador la resuelve segun el idioma del usuario
    public SinClavesDisponiblesException(String titulo) {
        super("pago.error.sinClaves");
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}