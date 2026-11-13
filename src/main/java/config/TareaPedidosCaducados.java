/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import service.interfaces.PagoService;

/**
 *
 * @author Sandy
 */
@Component
public class TareaPedidosCaducados {
// Es como un hilo, pero lo crea el propio Spring: yo solo digo que metodo y cada cuanto
// (@Scheduled), y Spring lo llama solo cada 5 minutos, sin new Thread ni nada de eso.
// Para que funcione hace falta @EnableScheduling en la clase principal

    @Autowired
    private PagoService pagoService;

    // Cada 5 minutos (300000 ms), contados desde que termina la anterior
    @Scheduled(fixedDelay = 300000)
    public void cancelarCaducados() {
        pagoService.cancelarPedidosCaducados();
    }
}
