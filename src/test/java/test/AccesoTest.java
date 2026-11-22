package test;

import controller.ControllerLogin;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import service.interfaces.ClienteService;
import service.interfaces.CuentaService;
import service.interfaces.UsuarioService;
import service.interfaces.VideojuegoService;

@WebMvcTest(ControllerLogin.class)
@ContextConfiguration(classes = AplicacionDeTest.class)
class AccesoTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioDao usuarioDao;

    @MockitoBean
    private ImagenUsuarioDao imagenUsuarioDao;

    @MockitoBean
    private VideojuegoService videojuegoService;

    @MockitoBean
    private CuentaService cuentaService;

    @Test
    void sinSesionLaRaizPideIdentificarse() {

        assertThat(mvc.get().uri("/"))
                .hasStatus3xxRedirection()
                .redirectedUrl().contains("/login");
    }

    @Test
    void elRegistroEsPublico() {

        assertThat(mvc.get().uri("/registro").locale(Locale.of("es")))
                .hasStatusOk()
                .hasViewName("login/crearCuenta");
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void elClienteQueEscribeElPanelAcabaEnLaTienda() {

        assertThat(mvc.get().uri("/panelControl"))
                .hasStatus3xxRedirection()
                .redirectedUrl().isEqualTo("/tienda");
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void elColaboradorTambienAcabaEnLaTienda() {

        assertThat(mvc.get().uri("/panelControl"))
                .hasStatus3xxRedirection()
                .redirectedUrl().isEqualTo("/tienda");
    }

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void soloElAdminEntraEnEmpleados() {

        assertThat(mvc.get().uri("/empleados")).hasStatus(403);
    }

    @Test
    @WithMockUser(roles = "GESTOR_PRODUCTOS")
    void elGestorDeProductosNoVeLasClavesDeLicencia() {

        assertThat(mvc.get().uri("/inventario/claves")).hasStatus(403);
    }
}
