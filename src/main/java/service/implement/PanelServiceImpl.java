package service.implement;

import dao.ClaveLicenciaDao;
import dao.PedidoDao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import model.inventario.EstadoClave;
import model.ventas.EstadoPedido;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.PanelService;

@Service
@RequiredArgsConstructor
public class PanelServiceImpl implements PanelService {

    private static final int TOP = 10;

    private final PedidoDao pedidoDao;
    private final ClaveLicenciaDao claveLicenciaDao;

    // Las siete consultas en una sola transaccion de lectura, de la misma foto de la base
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> resumen() {

        // La venta sin IVA de los pedidos PAGADOS: el IVA no es ingreso, se le debe a Hacienda
        BigDecimal ingresos = ceroSiNulo(pedidoDao.sumarBaseImponiblePorEstado(EstadoPedido.PAGADO));
        // Lo que costaron esas claves al comprarlas: el coste unitario de su linea de compra
        BigDecimal coste = ceroSiNulo(claveLicenciaDao.sumarCostePorEstado(EstadoClave.VENDIDA));

        Map<String, Object> resumen = new LinkedHashMap<>();

        resumen.put("ingresos", ingresos);
        resumen.put("coste", coste);
        resumen.put("margen", ingresos.subtract(coste));
        resumen.put("unidades", pedidoDao.contarUnidadesVendidas(EstadoPedido.PAGADO));
        resumen.put("pedidos", pedidoDao.countByEstado(EstadoPedido.PAGADO));
        // Los de hoy son los de este estado desde las 00:00, no los de las ultimas 24 horas
        resumen.put("pedidosHoy", pedidoDao.contarPorEstadoDesde(EstadoPedido.PAGADO,
                LocalDate.now().atStartOfDay()));
        resumen.put("clavesDisponibles", claveLicenciaDao.contarPorEstado(EstadoClave.DISPONIBLE));
        resumen.put("juegosConStock", claveLicenciaDao.contarJuegosConClavesEn(EstadoClave.DISPONIBLE));
        // JPQL no tiene LIMIT: el PageRequest es el que recorta la consulta a los diez primeros
        resumen.put("top", pedidoDao.topVendidos(EstadoPedido.PAGADO, PageRequest.of(0, TOP)));

        return resumen;
    }

    // Un sum() sin ninguna fila que sumar devuelve null, no cero. Con la base recien creada
    // eso llegaria a la plantilla y reventaria al formatear el importe.
    private BigDecimal ceroSiNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
