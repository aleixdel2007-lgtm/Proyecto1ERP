package service.interfaces;

import java.util.List;
import java.util.Map;
import model.catalogo.Categoria;

public interface CategoriaService {

    List<Categoria> listarCategorias(String texto);

    Categoria buscarCategoriaXId(Long id);

    Categoria guardarCategoria(Categoria categoria);

    void eliminarCategoria(Long id);

    Map<Long, Long> contarJuegos();

    long contarJuegosXCategoria(Long id);
}
