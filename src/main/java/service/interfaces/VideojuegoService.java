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
import model.publicacion.VersionJuego;
import org.springframework.web.multipart.MultipartFile;

public interface VideojuegoService {

    // Los videojuegos que publicamos nosotros, los que vienen de un proveedor
    List<Videojuego> listarVideojuegosPropios(String texto, Long categoriaId, Long plataformaId,
                                              BigDecimal precioMin, BigDecimal precioMax, Boolean activo);

    // Los que ha subido un colaborador
    List<Videojuego> listarVideojuegosDeColaboradores(String texto, Long categoriaId, Long plataformaId,
                                                      BigDecimal precioMin, BigDecimal precioMax, Boolean activo);

    Videojuego buscarVideojuegoXId(Long id);

    List<Videojuego> listarJuegosDeColaborador(String email);

    // id del juego -> su version activa; los que no tienen ninguna no salen en el mapa
    Map<Long, VersionJuego> listarVersionesActivas(List<Videojuego> videojuegos);

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
    List<String> listarPegis();

    // Valida la ficha que rellena un colaborador. Lanza IllegalArgumentException con el
    // mensaje que ve el usuario si algo no cuadra; no guarda nada
    void validarFichaColaborador(String titulo, String sinopsis, String pegi, String version,
                                 List<Long> categoriaIds, List<Long> plataformaIds);

    // Crea el juego de un colaborador con su ZIP y su portada. Nace INACTIVO: no sale en la
    // tienda hasta que alguien lo active. Valida todo antes de tocar nada y, si algo falla
    // a medias, deshace la base de datos y borra los archivos que hubiera escrito
    Videojuego publicarJuegoColaborador(String emailColaborador, String titulo, String sinopsis,
                                        String pegi, String version,
                                        List<Long> categoriaIds, List<Long> plataformaIds,
                                        MultipartFile zip, MultipartFile portada,
                                        MultipartFile banner, List<MultipartFile> capturas);

    Videojuego buscarJuegoDeColaborador(String emailColaborador, Long videojuegoId);

    VersionJuego buscarVersionActiva(Long videojuegoId);

    Videojuego actualizarJuegoColaborador(String emailColaborador, Long videojuegoId, String titulo,
                                          String sinopsis, String pegi, String version,
                                          List<Long> categoriaIds, List<Long> plataformaIds,
                                          MultipartFile zip, MultipartFile portada,
                                          MultipartFile banner, List<MultipartFile> capturas);

    void eliminarImagenDeColaborador(String emailColaborador, Long videojuegoId, Long imagenId);
}
