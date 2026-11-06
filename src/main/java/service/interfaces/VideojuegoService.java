package service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Categoria;
import model.catalogo.Plataforma;
import model.catalogo.Videojuego;

public interface VideojuegoService {

    List<Videojuego> listarVideojuegos(String texto, Long categoriaId, Long plataformaId,
                                       BigDecimal precioMin, BigDecimal precioMax, Boolean activo);

    Videojuego buscarVideojuegoXId(Long id);

    Videojuego guardarVideojuego(Videojuego videojuego, Long proveedorId,
                                 List<Long> categoriaIds, List<Long> plataformaIds);

    void eliminarVideojuego(Long id);

    List<Proveedor> listarProveedoresActivos();

    List<Categoria> listarCategorias();

    List<Plataforma> listarPlataformas();
}
