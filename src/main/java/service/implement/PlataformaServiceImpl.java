package service.implement;

import dao.PlataformaDao;
import dao.VideojuegoDao;
import exception.CodigoPlataformaDuplicadoException;
import exception.NombrePlataformaDuplicadoException;
import exception.PlataformaConJuegosException;
import exception.PlataformaNoEncontradaException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.catalogo.Plataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import service.interfaces.PlataformaService;

@Service
public class PlataformaServiceImpl implements PlataformaService {

    @Autowired
    private PlataformaDao plataformaDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Override
    public List<Plataforma> listarPlataformas(String texto) {
        return plataformaDao.listarFiltrado(normalizarTexto(texto));
    }

    @Override
    public Plataforma buscarPlataformaXId(Long id) {

        Plataforma plataforma = plataformaDao.findById(id).orElse(null);

        if (plataforma == null) {
            throw new PlataformaNoEncontradaException(id);
        }
        return plataforma;
    }

    @Override
    public Plataforma guardarPlataforma(Plataforma plataforma) {

        validarObligatorios(plataforma);
        normalizarCampos(plataforma);
        validarCodigoRepetido(plataforma);
        validarNombreRepetido(plataforma);

        return plataformaDao.save(plataforma);
    }

    private void validarObligatorios(Plataforma plataforma) {

        if (plataforma.getCodigo() == null || plataforma.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }

        if (plataforma.getNombre() == null || plataforma.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    private void normalizarCampos(Plataforma plataforma) {

        plataforma.setCodigo(plataforma.getCodigo().trim().replace(" ", "").toUpperCase());
        plataforma.setNombre(plataforma.getNombre().trim());
    }

    private void validarCodigoRepetido(Plataforma plataforma) {

        boolean repetido;

        if (plataforma.getId() == null) {
            repetido = plataformaDao.existsByCodigoIgnoreCase(plataforma.getCodigo());
        } else {
            repetido = plataformaDao.existsByCodigoIgnoreCaseAndIdNot(
                    plataforma.getCodigo(), plataforma.getId());
        }

        if (repetido) {
            throw new CodigoPlataformaDuplicadoException();
        }
    }

    private void validarNombreRepetido(Plataforma plataforma) {

        boolean repetido;

        if (plataforma.getId() == null) {
            repetido = plataformaDao.existsByNombreIgnoreCase(plataforma.getNombre());
        } else {
            repetido = plataformaDao.existsByNombreIgnoreCaseAndIdNot(
                    plataforma.getNombre(), plataforma.getId());
        }

        if (repetido) {
            throw new NombrePlataformaDuplicadoException();
        }
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    @Override
    public void eliminarPlataforma(Long id) {

        Plataforma plataforma = buscarPlataformaXId(id);

        if (videojuegoDao.countByPlataformasId(id) > 0) {
            throw new PlataformaConJuegosException();
        }

        try {
            plataformaDao.delete(plataforma);
            plataformaDao.flush();

        } catch (DataIntegrityViolationException e) {
            throw new PlataformaConJuegosException();
        }
    }

    @Override
    public Map<Long, Long> contarJuegos() {

        Map<Long, Long> cuenta = new HashMap<>();

        for (Object[] fila : videojuegoDao.contarPorPlataforma()) {
            cuenta.put((Long) fila[0], (Long) fila[1]);
        }
        return cuenta;
    }

    @Override
    public long contarJuegosXPlataforma(Long id) {
        return videojuegoDao.countByPlataformasId(id);
    }
}
