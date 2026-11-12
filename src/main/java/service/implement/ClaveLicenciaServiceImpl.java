package service.implement;

import config.IndiceCiego;
import dao.ClaveLicenciaDao;
import dao.VideojuegoDao;
import exception.clave.ClaveNoAnulableException;
import exception.clave.ClaveNoEncontradaException;
import java.time.LocalDateTime;
import java.util.List;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ClaveLicenciaService;

@Service
@Transactional(readOnly = true)
public class ClaveLicenciaServiceImpl implements ClaveLicenciaService {

    private static final int LARGO_MOTIVO = 255;

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

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
}
