/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.interfaces;

import model.ventas.Cesta;

/**
 *
 * @author Sandy
 */
public interface CestaService {

    void anadirVideojuego(String email, Long videojuegoId);

    Cesta buscarOCrearCesta(String email);

    void cambiarCantidad(String email, Long lineaId, int cantidad);

    void quitarLinea(String email, Long lineaId);

    void vaciarCesta(String email);

}
