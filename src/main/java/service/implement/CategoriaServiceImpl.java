package service.implement;

import dao.CategoriaDao;
import dao.VideojuegoDao;
import exception.categoria.CategoriaConJuegosException;
import exception.categoria.CategoriaNoEncontradaException;
import exception.categoria.CodigoCategoriaDuplicadoException;
import exception.categoria.NombreCategoriaDuplicadoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.catalogo.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import service.interfaces.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaDao categoriaDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Override
    public List<Categoria> listarCategorias(String texto) {
        return categoriaDao.listarFiltrado(normalizarTexto(texto));
    }

    @Override
    public Categoria buscarCategoriaXId(Long id) {

        Categoria categoria = categoriaDao.findById(id).orElse(null);

        if (categoria == null) {
            throw new CategoriaNoEncontradaException(id);
        }
        return categoria;
    }

    @Override
    public Categoria guardarCategoria(Categoria categoria) {

        validarObligatorios(categoria);
        normalizarCampos(categoria);
        validarCodigoRepetido(categoria);
        validarNombreRepetido(categoria);

        return categoriaDao.save(categoria);
    }

    private void validarObligatorios(Categoria categoria) {

        if (categoria.getCodigo() == null || categoria.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }

        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    private void normalizarCampos(Categoria categoria) {

        categoria.setCodigo(categoria.getCodigo().trim().replace(" ", "").toUpperCase());
        categoria.setNombre(categoria.getNombre().trim());
    }

    private void validarCodigoRepetido(Categoria categoria) {

        boolean repetido;

        if (categoria.getId() == null) {
            repetido = categoriaDao.existsByCodigoIgnoreCase(categoria.getCodigo());
        } else {
            repetido = categoriaDao.existsByCodigoIgnoreCaseAndIdNot(
                    categoria.getCodigo(), categoria.getId());
        }

        if (repetido) {
            throw new CodigoCategoriaDuplicadoException();
        }
    }

    private void validarNombreRepetido(Categoria categoria) {

        boolean repetido;

        if (categoria.getId() == null) {
            repetido = categoriaDao.existsByNombreIgnoreCase(categoria.getNombre());
        } else {
            repetido = categoriaDao.existsByNombreIgnoreCaseAndIdNot(
                    categoria.getNombre(), categoria.getId());
        }

        if (repetido) {
            throw new NombreCategoriaDuplicadoException();
        }
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    @Override
    public void eliminarCategoria(Long id) {

        Categoria categoria = buscarCategoriaXId(id);

        if (videojuegoDao.countByCategoriasId(id) > 0) {
            throw new CategoriaConJuegosException();
        }

        try {
            categoriaDao.delete(categoria);
            categoriaDao.flush();

        } catch (DataIntegrityViolationException e) {
            throw new CategoriaConJuegosException();
        }
    }

    @Override
    public Map<Long, Long> contarJuegos() {

        Map<Long, Long> cuenta = new HashMap<>();

        for (Object[] fila : videojuegoDao.contarPorCategoria()) {
            cuenta.put((Long) fila[0], (Long) fila[1]);
        }
        return cuenta;
    }

    @Override
    public long contarJuegosXCategoria(Long id) {
        return videojuegoDao.countByCategoriasId(id);
    }
}
