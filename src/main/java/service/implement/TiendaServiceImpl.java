package service.implement;

import dao.CategoriaDao;
import dao.ImagenVideojuegoDao;
import dao.PlataformaDao;
import dao.VideojuegoDao;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.catalogo.Categoria;
import model.catalogo.ImagenVideojuego;
import model.catalogo.Plataforma;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.interfaces.TiendaService;

@Service
public class TiendaServiceImpl implements TiendaService {

    private static final String ORDEN_PRECIO_ASC = "precioAsc";
    private static final String ORDEN_PRECIO_DESC = "precioDesc";
    private static final String ORDEN_NOVEDADES = "novedades";

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private ImagenVideojuegoDao imagenVideojuegoDao;

    @Autowired
    private CategoriaDao categoriaDao;

    @Autowired
    private PlataformaDao plataformaDao;

    @Override
    public List<Videojuego> listarCatalogo(String texto, Long categoriaId, Long plataformaId,
                                           BigDecimal precioMin, BigDecimal precioMax, String orden) {

        return buscar(texto, categoriaId, plataformaId, precioMin, precioMax, orden, false);
    }

    @Override
    public List<Videojuego> listarOfertas(String texto, Long categoriaId, Long plataformaId,
                                          BigDecimal precioMin, BigDecimal precioMax, String orden) {

        return buscar(texto, categoriaId, plataformaId, precioMin, precioMax, orden, true);
    }

    @Override
    public List<Videojuego> listarJuegosColaboradores() {

        List<Videojuego> colaboradores = videojuegoDao.listarCatalogo(
                null, null, null, null, null, true, false);

        return ordenar(conClasificacion(colaboradores), ORDEN_NOVEDADES);
    }

    @Override
    public Videojuego buscarVideojuegoActivoXId(Long id) {

        Videojuego videojuego = videojuegoDao.buscarActivoConDetalle(id).orElse(null);

        if (videojuego == null) {
            throw new VideojuegoNoEncontradoException(id);
        }
        return videojuego;
    }

    @Override
    public Map<Long, String> listarPortadas(List<Videojuego> videojuegos) {

        Map<Long, String> portadas = new HashMap<>();

        if (videojuegos == null || videojuegos.isEmpty()) {
            return portadas;
        }

        List<Long> ids = new ArrayList<>();

        for (Videojuego videojuego : videojuegos) {
            ids.add(videojuego.getId());
        }

        for (Object[] fila : imagenVideojuegoDao.buscarPorTipo(ids, TipoImagen.PORTADA)) {
            portadas.putIfAbsent((Long) fila[0], (String) fila[1]);
        }
        return portadas;
    }

    @Override
    public List<ImagenVideojuego> listarImagenes(Long videojuegoId) {
        return imagenVideojuegoDao.findByVideojuegoIdOrderByOrdenAscIdAsc(videojuegoId);
    }

    @Override
    public List<Categoria> listarCategorias() {
        return categoriaDao.listarFiltrado(null);
    }

    @Override
    public List<Plataforma> listarPlataformas() {
        return plataformaDao.listarFiltrado(null);
    }

    private List<Videojuego> buscar(String texto, Long categoriaId, Long plataformaId,
                                    BigDecimal precioMin, BigDecimal precioMax, String orden,
                                    boolean soloOfertas) {

        List<Videojuego> filtrados = videojuegoDao.listarCatalogo(
                normalizarTexto(texto), categoriaId, plataformaId, precioMin, precioMax,
                null, soloOfertas);

        return ordenar(conClasificacion(filtrados), orden);
    }

    private List<Videojuego> conClasificacion(List<Videojuego> videojuegos) {

        if (videojuegos.isEmpty()) {
            return videojuegos;
        }

        List<Long> ids = new ArrayList<>();

        for (Videojuego videojuego : videojuegos) {
            ids.add(videojuego.getId());
        }
        return videojuegoDao.listarConClasificacion(ids);
    }

    private List<Videojuego> ordenar(List<Videojuego> videojuegos, String orden) {

        if (orden == null || videojuegos.isEmpty()) {
            return videojuegos;
        }

        Comparator<Videojuego> porPrecio = Comparator.comparing(TiendaServiceImpl::precioEfectivo);
        List<Videojuego> ordenados = new ArrayList<>(videojuegos);

        if (ORDEN_PRECIO_ASC.equals(orden)) {
            ordenados.sort(porPrecio);

        } else if (ORDEN_PRECIO_DESC.equals(orden)) {
            ordenados.sort(porPrecio.reversed());

        } else if (ORDEN_NOVEDADES.equals(orden)) {
            ordenados.sort(Comparator.comparing(Videojuego::getFechaLanzamiento,
                    Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())));

        } else {
            return videojuegos;
        }
        return ordenados;
    }

    private static BigDecimal precioEfectivo(Videojuego videojuego) {

        return videojuego.getPrecioDescuento() != null
                ? videojuego.getPrecioDescuento() : videojuego.getPrecio();
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
