package service.interfaces;

import java.util.Map;

public interface PanelService {

    // Las cifras del panel de control, listas para meterlas en el modelo con addAllAttributes.
    // El mapa trae ocho numeros (ingresos, coste, margen, unidades, pedidos, pedidosHoy,
    // clavesDisponibles y juegosConStock) y la lista "top", donde cada fila es un Object[]
    // con el titulo del juego, las unidades vendidas y el importe. No se guarda nada: las
    // consultas se repiten en cada visita, asi que lo que se ve es lo que hay en la base.
    Map<String, Object> resumen();
}
