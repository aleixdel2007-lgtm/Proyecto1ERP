/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception;

/**
 *
 * @author Sandy
 */
public class ClienteConDatosAsociadosException extends RuntimeException {

    public ClienteConDatosAsociadosException() {
        super("No se puede eliminar este cliente porque tiene datos asociados (pedidos, cesta o claves). Puedes inactivarlo.");
    }

}
