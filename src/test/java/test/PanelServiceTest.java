package test;

import dao.ClaveLicenciaDao;
import dao.PedidoDao;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import model.inventario.EstadoClave;
import model.ventas.EstadoPedido;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import service.implement.PanelServiceImpl;

@ExtendWith(MockitoExtension.class)
class PanelServiceTest {

    @Mock
    private PedidoDao pedidoDao;

    @Mock
    private ClaveLicenciaDao claveLicenciaDao;

    @InjectMocks
    private PanelServiceImpl panelService;

    @BeforeEach
    void unaVentaDeCincuentaEurosSinIva() {
        given(pedidoDao.sumarBaseImponiblePorEstado(EstadoPedido.PAGADO)).willReturn(new BigDecimal("50.00"));
        given(claveLicenciaDao.sumarCostePorEstado(EstadoClave.VENDIDA)).willReturn(new BigDecimal("35.00"));
    }

    @Test
    void elMargenEsLaVentaSinIvaMenosElCoste() {

        Map<String, Object> resumen = panelService.resumen();

        assertThat(resumen.get("ingresos")).isEqualTo(new BigDecimal("50.00"));
        assertThat(resumen.get("coste")).isEqualTo(new BigDecimal("35.00"));
        assertThat(resumen.get("margen")).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    void elIngresoSaleDeLaBaseImponibleYNoDelTotalConIva() {

        panelService.resumen();

        verify(pedidoDao).sumarBaseImponiblePorEstado(EstadoPedido.PAGADO);
    }

    @Test
    void sinVentasLasSumasNulasSeVenComoCero() {

        given(pedidoDao.sumarBaseImponiblePorEstado(EstadoPedido.PAGADO)).willReturn(null);
        given(claveLicenciaDao.sumarCostePorEstado(EstadoClave.VENDIDA)).willReturn(null);

        Map<String, Object> resumen = panelService.resumen();

        assertThat(resumen.get("ingresos")).isEqualTo(BigDecimal.ZERO);
        assertThat(resumen.get("coste")).isEqualTo(BigDecimal.ZERO);
        assertThat(resumen.get("margen")).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void elTopSePideDeDiezEnDiez() {

        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);

        panelService.resumen();

        verify(pedidoDao).topVendidos(eq(EstadoPedido.PAGADO), pagina.capture());
        assertThat(pagina.getValue().getPageSize()).isEqualTo(10);
        assertThat(pagina.getValue().getPageNumber()).isZero();
    }

    @Test
    void elResumenTraeLasNueveEntradasQueLeeLaPlantilla() {

        given(pedidoDao.topVendidos(any(), any())).willReturn(List.<Object[]>of(
                new Object[]{"Eclipse Protocol", 96L, new BigDecimal("4319.04")}));

        Map<String, Object> resumen = panelService.resumen();

        assertThat(resumen).containsOnlyKeys("ingresos", "coste", "margen", "unidades",
                "pedidos", "pedidosHoy", "clavesDisponibles", "juegosConStock", "top");
        assertThat((List<?>) resumen.get("top")).hasSize(1);
    }
}
