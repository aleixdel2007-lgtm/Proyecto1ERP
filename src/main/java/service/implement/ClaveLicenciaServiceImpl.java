package service.implement;

import config.IndiceCiego;
import dao.ClaveLicenciaDao;
import dao.EmpleadoDao;
import dao.VideojuegoDao;
import exception.clave.ClaveNoAnulableException;
import exception.clave.ClaveNoEncontradaException;
import java.time.LocalDateTime;
import java.util.List;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import model.seguridad.Empleado;
import model.seguridad.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ClaveLicenciaService;
import service.interfaces.EmailService;

@Service
@Transactional(readOnly = true)
public class ClaveLicenciaServiceImpl implements ClaveLicenciaService {

    private static final int LARGO_MOTIVO = 255;

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao;

    @Autowired
    private VideojuegoDao videojuegoDao;
    @Autowired
    private EmpleadoDao empleadoDao;

    @Autowired
    private EmailService emailService; //el servidor de correo

    // Si quedan estas claves o menos, se avisa a los gestores de inventario
    private static final long UMBRAL_STOCK_BAJO = 3;

    // Las claves del panel. El codigo escrito se convierte en hash antes de bajar al DAO
    @Override
    public List<ClaveLicencia> listarClaves(String codigo, Long videojuegoId, EstadoClave estado) {
        return claveLicenciaDao.listarFiltrado(hashDe(codigo), videojuegoId, estado);
    }

    // Cuantas claves hay en un estado, para los contadores de la cabecera
    @Override
    public long contarPorEstado(EstadoClave estado) {
        return claveLicenciaDao.contarPorEstado(estado);
    }

    // Los videojuegos del desplegable del filtro, por titulo
    @Override
    public List<Videojuego> listarVideojuegos() {
        return videojuegoDao.findAll(Sort.by("titulo"));
    }

    // Una clave con su juego, su cliente y su compra ya cargados, o 404 si no existe
    @Override
    public ClaveLicencia buscarClaveXId(Long id) {

        ClaveLicencia clave = claveLicenciaDao.buscarConDetalle(id).orElse(null);

        if (clave == null) {
            throw new ClaveNoEncontradaException(id);
        }
        return clave;
    }

    // Deja una clave DISPONIBLE inservible para siempre: no se borra la fila, se marca
    @Override
    @Transactional
    public void anularClave(Long id, String motivo) {

        ClaveLicencia clave = buscarClaveXId(id);

        // El unico guardia de la transicion: la base no impide ningun cambio de estado
        if (clave.getEstado() != EstadoClave.DISPONIBLE) {
            throw new ClaveNoAnulableException(clave.getEstado());
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la anulacion es obligatorio");
        }

        if (motivo.trim().length() > LARGO_MOTIVO) {
            throw new IllegalArgumentException("El motivo no puede pasar de " + LARGO_MOTIVO + " caracteres");
        }

        clave.setEstado(EstadoClave.ANULADA);
        clave.setFechaAnulacion(LocalDateTime.now());
        clave.setMotivoAnulacion(motivo.trim());

        claveLicenciaDao.save(clave);
    }

    // El codigo se busca por su hash, nunca por la columna cifrada; vacio apaga el filtro
    private String hashDe(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return IndiceCiego.hash(codigo);
    }

    @Override
    public void avisarSiQuedaPocoStock(Videojuego videojuego) {

        long quedan = claveLicenciaDao.contarPorVideojuegoYEstado(
                videojuego.getId(), EstadoClave.DISPONIBLE);

        if (quedan > UMBRAL_STOCK_BAJO) {
            return;
        }

        List<Empleado> gestores = empleadoDao.listarFiltrado(null, null, Rol.GESTOR_INVENTARIO);

        for (Empleado gestor : gestores) {
            emailService.enviarAvisoStockBajo(gestor.getEmail(), videojuego.getTitulo(), quedan);
        }
    }
}
