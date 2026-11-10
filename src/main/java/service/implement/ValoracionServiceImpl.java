package service.implement;

import dao.ClienteDao;
import dao.ValoracionDao;
import dao.VideojuegoDao;
import exception.usuario.UsuarioNoEncontradoException;
import exception.valoracion.ValoracionDuplicadaException;
import exception.valoracion.ValoracionInvalidaException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.util.List;
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
        if (titulo == null || titulo.isBlank()) {
            throw new ValoracionInvalidaException("El título es obligatorio");
        }
        if (titulo.trim().length() > 150) {
            throw new ValoracionInvalidaException("El título no puede pasar de 150 caracteres");
        }
        if (puntuacion < 1 || puntuacion > 5) {
            throw new ValoracionInvalidaException("La valoración debe estar entre 1 y 5");
        }

        if (valoracionDao.existsByClienteEmailAndVideojuegoId(emailCliente, videojuegoId)) {
            throw new ValoracionDuplicadaException(videojuegoId);
        }

        Valoracion v = new Valoracion();
        v.setCliente(clienteDao.findByEmail(emailCliente)
                .orElseThrow(() -> new UsuarioNoEncontradoException(emailCliente)));
        v.setVideojuego(videojuegoDao.findById(videojuegoId)
                .orElseThrow(() -> new VideojuegoNoEncontradoException(videojuegoId)));
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
}
