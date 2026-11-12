/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.interfaces;

import model.ventas.Pedido;

/**
 *
 * @author Sandy
 */
public interface PagoService {

    // Crea el pedido desde la cesta y la sesion de pago en Stripe.
    // Devuelve la direccion de la pagina de pago de Stripe
    String iniciarPago(String email);

    // Pregunta a Stripe si esa sesion esta pagada y, si lo esta, cierra la compra
    Pedido confirmarPago(String email, String sessionId);
}
