package test;

import controller.ControllerCliente;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import exception.cliente.MotivoInactivacionObligatorioException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import service.interfaces.ClienteService;
import service.interfaces.UsuarioService;
import service.interfaces.VideojuegoService;

@WebMvcTest(ControllerCliente.class)
@ContextConfiguration(classes = AplicacionDeTest.class)
class ClientesTest {

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

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void inactivarConMotivoLoPasaAlServicio() {

        assertThat(mvc.post().uri("/clientes/5/estado")
                .param("activo", "false")
                .param("motivo", "pagos rechazados")
                .with(csrf()))
                .hasStatus3xxRedirection()
                .redirectedUrl().isEqualTo("/clientes");

        verify(clienteService).cambiarEstado(5L, false, "pagos rechazados");
    }

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void inactivarSinMotivoVuelveAlListadoSinCambiarNada() {

        willThrow(new MotivoInactivacionObligatorioException())
                .given(clienteService).cambiarEstado(5L, false, null);

        assertThat(mvc.post().uri("/clientes/5/estado")
                .param("activo", "false")
                .with(csrf()))
                .hasStatus3xxRedirection()
                .redirectedUrl().isEqualTo("/clientes");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activarNoNecesitaMotivo() {

        assertThat(mvc.post().uri("/clientes/5/estado")
                .param("activo", "true")
                .with(csrf()))
                .hasStatus3xxRedirection();

        verify(clienteService).cambiarEstado(5L, true, null);
    }

    @Test
    @WithMockUser(roles = "GESTOR_PRODUCTOS")
    void elGestorDeProductosNoTocaClientes() {

        assertThat(mvc.post().uri("/clientes/5/estado")
                .param("activo", "false")
                .param("motivo", "lo que sea")
                .with(csrf()))
                .hasStatus(403);
    }

    @Test
    @WithMockUser(roles = "GESTOR_CLIENTES")
    void elListadoDeClientesSePinta() {

        given(clienteService.buscarFiltrado(null, null, null)).willReturn(List.of());
        given(clienteService.listarImagenesPerfil(List.of())).willReturn(Map.of());
        given(clienteService.listarPaises()).willReturn(List.of("España"));

        assertThat(mvc.get().uri("/clientes").locale(Locale.of("es")))
                .hasStatusOk()
                .hasViewName("gestorCliente/listadoClientes")
                .bodyText().doesNotContain("??clientes.");
    }
}
