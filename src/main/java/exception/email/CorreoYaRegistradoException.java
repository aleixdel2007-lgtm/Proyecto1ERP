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
        super("Este correo ya tiene una cuenta registrada. Prueba con otro o inicia sesión.");
    }
}
