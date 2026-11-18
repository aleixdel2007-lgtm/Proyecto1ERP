package test;

import controller.ControllerPanelControl;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import service.interfaces.ClienteService;
import service.interfaces.PanelService;
import service.interfaces.UsuarioService;
import service.interfaces.VideojuegoService;

@WebMvcTest(ControllerPanelControl.class)
@ContextConfiguration(classes = AplicacionDeTest.class)
class PanelControlTest {

    private static final Locale CASTELLANO = Locale.of("es");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PanelService panelService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioDao usuarioDao;

    @MockitoBean
    private ImagenUsuarioDao imagenUsuarioDao;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private VideojuegoService videojuegoService;

    @BeforeEach
    void cifrasConocidas() {
        given(panelService.resumen()).willReturn(resumen(List.of(
                new Object[]{"Eclipse Protocol", 96L, new BigDecimal("4319.04")},
                new Object[]{"Ashen Crown", 48L, new BigDecimal("5669.19")})));
    }

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void elPanelPintaLasCifras() {

        assertThat(mvc.get().uri("/panelControl").locale(CASTELLANO))
                .hasStatusOk()
                .hasViewName("panelControl")
                .bodyText()
                .contains("Ingresos", "Coste", "Margen")
                .contains("50,00 €")
                .contains("35,00 €")
                .contains("15,00 €")
                .contains("1.187")
                .contains("31 hoy")
                .contains("en 6 juegos");
    }

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void elRankingPintaLasBarrasYLosImportes() {

        assertThat(mvc.get().uri("/panelControl").locale(CASTELLANO))
                .hasStatusOk()
                .bodyText()
                .contains("Eclipse Protocol")
                .contains("Ashen Crown")
                .contains("width:100%")
                .contains("width:50%")
                .contains("96 ud · 4.319,04 €");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sinVentasSaleElAvisoYNoHayFilas() {

        given(panelService.resumen()).willReturn(resumen(List.of()));

        assertThat(mvc.get().uri("/panelControl").locale(CASTELLANO))
                .hasStatusOk()
                .bodyText()
                .contains("Todavía no hay pedidos pagados")
                .doesNotContain("rank-row");
    }

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void lasClavesDeTraduccionExistenEnLosTresIdiomas() {

        for (String idioma : List.of("es", "en", "ca")) {
            assertThat(mvc.get().uri("/panelControl").locale(Locale.of(idioma)))
                    .hasStatusOk()
                    .bodyText()
                    .doesNotContain("??panel.inicio");
        }
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteNoEntraEnElPanel() {

        assertThat(mvc.get().uri("/panelControl")).hasStatus3xxRedirection();
    }

    @Test
    void sinSesionTeMandaAlLogin() {

        assertThat(mvc.get().uri("/panelControl"))
                .hasStatus3xxRedirection()
                .redirectedUrl().contains("/login");
    }

    private Map<String, Object> resumen(List<Object[]> top) {

        Map<String, Object> resumen = new LinkedHashMap<>();

        resumen.put("ingresos", new BigDecimal("50.00"));
        resumen.put("coste", new BigDecimal("35.00"));
        resumen.put("margen", new BigDecimal("15.00"));
        resumen.put("unidades", 1187L);
        resumen.put("pedidos", 904L);
        resumen.put("pedidosHoy", 31L);
        resumen.put("clavesDisponibles", 1106L);
        resumen.put("juegosConStock", 6L);
        resumen.put("top", top);

        return resumen;
    }
}
