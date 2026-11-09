/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception.cesta;

/**
 *
 * @author Sandy
 */
public class LineaCestaNoEncontradaException extends RuntimeException {

    public LineaCestaNoEncontradaException(Long id) {
        super("No existe la linea de cesta " + id);
    }
}
