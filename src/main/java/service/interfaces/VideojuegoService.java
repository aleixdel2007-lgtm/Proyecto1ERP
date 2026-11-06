package service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Categoria;
import model.catalogo.ImagenVideojuego;
import model.catalogo.Plataforma;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
import org.springframework.web.multipart.MultipartFile;

public interface VideojuegoService {

    List<Videojuego> listarVideojuegos(String texto, Long categoriaId, Long plataformaId,
                                       BigDecimal precioMin, BigDecimal precioMax, Boolean activo);

    Videojuego buscarVideojuegoXId(Long id);

    Videojuego guardarVideojuego(Videojuego videojuego, Long proveedorId,
                                 List<Long> categoriaIds, List<Long> plataformaIds);

    void eliminarVideojuego(Long id);

    List<ImagenVideojuego> listarImagenes(Long videojuegoId);

    Map<Long, String> listarPortadas(List<Videojuego> videojuegos);

    ImagenVideojuego guardarImagen(Long videojuegoId, MultipartFile archivo,
                                   TipoImagen tipo, String textoAlternativo);

    void eliminarImagen(Long videojuegoId, Long imagenId);

    List<Proveedor> listarProveedoresActivos();

    List<Categoria> listarCategorias();

    List<Plataforma> listarPlataformas();
}
