package service.implement;

import dao.ClaveLicenciaDao;
import dao.ClienteDao;
import dao.ValoracionDao;
import dao.VideojuegoDao;
import exception.usuario.UsuarioNoEncontradoException;
import exception.valoracion.ValoracionDuplicadaException;
import exception.valoracion.ValoracionInvalidaException;
import exception.valoracion.ValoracionSinCompraException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.util.List;
import model.catalogo.Videojuego;
import model.valoracion.EstadoValoracion;
import model.valoracion.Valoracion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ValoracionService;

@Service
@Transactional(readOnly = true)
public class ValoracionServiceImpl implements ValoracionService {

    @Autowired
    private ValoracionDao valoracionDao;

    @Autowired
    private ClienteDao clienteDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao;

    @Override
    public List<Valoracion> listarVisiblesPorVideojuego(Long videojuegoId) {
        return valoracionDao.listarVisiblesPorVideojuego(videojuegoId, EstadoValoracion.ACTIVA);
    }

    @Override
    public List<Valoracion> listarPorCliente(Long clienteId) {
        return valoracionDao.findByClienteId(clienteId);
    }

    @Override
    @Transactional
    public Valoracion crear(String emailCliente, Long videojuegoId, String titulo, String descripcion, int puntuacion) {
        Videojuego videojuego = videojuegoDao.findById(videojuegoId)
                .orElseThrow(() -> new VideojuegoNoEncontradoException(videojuegoId));

        if (!esGratis(videojuego)
                && !claveLicenciaDao.estaEnBiblioteca(emailCliente, videojuegoId)) {
            throw new ValoracionSinCompraException(videojuegoId);
        }
        if (titulo == null || titulo.isBlank()) {
            throw new ValoracionInvalidaException("valoraciones.error.tituloObligatorio");
        }
        if (titulo.trim().length() > 150) {
            throw new ValoracionInvalidaException("valoraciones.error.tituloLargo");
        }
        if (puntuacion < 1 || puntuacion > 5) {
            throw new ValoracionInvalidaException("valoraciones.error.puntuacionInvalida");
        }

        if (valoracionDao.existsByClienteEmailAndVideojuegoId(emailCliente, videojuegoId)) {
            throw new ValoracionDuplicadaException(videojuegoId);
        }

        Valoracion v = new Valoracion();
        v.setCliente(clienteDao.findByEmail(emailCliente)
                .orElseThrow(() -> new UsuarioNoEncontradoException(emailCliente)));
        v.setVideojuego(videojuego);
        v.setTitulo(titulo.trim());
        v.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
        v.setValoracion(puntuacion);
        v.setEstado(EstadoValoracion.ACTIVA);
        return valoracionDao.save(v);
    }

    @Override
    public boolean haValorado(String emailCliente, Long videojuegoId) {
        return valoracionDao.existsByClienteEmailAndVideojuegoId(emailCliente, videojuegoId);
    }

    // Un juego gratis se juega sin comprarlo, asi que tambien se valora sin tenerlo
    @Override
    public boolean puedeValorar(String emailCliente, Long videojuegoId) {

        Videojuego videojuego = videojuegoDao.findById(videojuegoId).orElse(null);

        if (videojuego == null) {
            return false;
        }
        return esGratis(videojuego)
                || claveLicenciaDao.estaEnBiblioteca(emailCliente, videojuegoId);
    }

    // El mismo criterio que la descarga en BibliotecaServiceImpl: manda el precio con descuento
    private boolean esGratis(Videojuego videojuego) {
        return (videojuego.getPrecioDescuento() != null
                ? videojuego.getPrecioDescuento()
                : videojuego.getPrecio()).signum() == 0;
    }
}
