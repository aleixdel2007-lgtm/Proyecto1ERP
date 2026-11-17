package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import service.interfaces.PagoService;

@Component
public class TareaPedidosCaducados {

    @Autowired
    private PagoService pagoService;

    // Tarea periodica que lanza Spring (necesita @EnableScheduling): 5 minutos tras terminar la anterior
    @Scheduled(fixedDelay = 300000)
    public void cancelarCaducados() {
        // Cancela los pedidos pendientes de pago que han caducado
        pagoService.cancelarPedidosCaducados();
    }
}
