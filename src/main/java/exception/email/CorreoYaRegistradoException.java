/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.email;

/**
 *
 * @author Sandy
 */
public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException() {
        // El mensaje es la clave de traduccion: el controlador la resuelve segun el idioma
        super("registro.error.correoEnUso");
    }
}
