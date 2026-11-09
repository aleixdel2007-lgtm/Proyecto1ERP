package service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import model.catalogo.Categoria;
import model.catalogo.ImagenVideojuego;
import model.catalogo.Plataforma;
import model.catalogo.Videojuego;

public interface TiendaService {

    List<Videojuego> listarCatalogo(String texto, Long categoriaId, Long plataformaId,
                                    BigDecimal precioMin, BigDecimal precioMax, String orden);

    List<Videojuego> listarOfertas(String texto, Long categoriaId, Long plataformaId,
                                   BigDecimal precioMin, BigDecimal precioMax, String orden);

    List<Videojuego> listarJuegosColaboradores();

    Videojuego buscarVideojuegoActivoXId(Long id);

    Map<Long, String> listarPortadas(List<Videojuego> videojuegos);

    List<ImagenVideojuego> listarImagenes(Long videojuegoId);

    List<Categoria> listarCategorias();

    List<Plataforma> listarPlataformas();
}
