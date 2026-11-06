package service.implement;

import dao.CategoriaDao;
import dao.PlataformaDao;
import dao.ProveedorDao;
import dao.VideojuegoDao;
import exception.precio.PrecioDescuentoInvalidoException;
import exception.precio.PrecioInvalidoException;
import exception.videojuego.ProveedorObligatorioException;
import exception.videojuego.VideojuegoConReferenciasException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Categoria;
import model.catalogo.Plataforma;
import model.catalogo.Videojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import service.interfaces.VideojuegoService;

@Service
public class VideojuegoServiceImpl implements VideojuegoService {

    private static final List<Integer> PEGI_VALIDOS = List.of(3, 7, 12, 16, 18);

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private ProveedorDao proveedorDao;

    @Autowired
    private CategoriaDao categoriaDao;

    @Autowired
    private PlataformaDao plataformaDao;

    @Override
    public List<Videojuego> listarVideojuegos(String texto, Long categoriaId, Long plataformaId,
                                              BigDecimal precioMin, BigDecimal precioMax, Boolean activo) {

        List<Videojuego> filtrados = videojuegoDao.listarFiltrado(
                normalizarTexto(texto), categoriaId, plataformaId, precioMin, precioMax, activo);

        if (filtrados.isEmpty()) {
            return filtrados;
        }

        List<Long> ids = new ArrayList<>();

        for (Videojuego videojuego : filtrados) {
            ids.add(videojuego.getId());
        }
        return videojuegoDao.listarConClasificacion(ids);
    }

    @Override
    public Videojuego buscarVideojuegoXId(Long id) {

        Videojuego videojuego = videojuegoDao.buscarConDetalle(id).orElse(null);

        if (videojuego == null) {
            throw new VideojuegoNoEncontradoException(id);
        }
        return videojuego;
    }

    @Override
    public Videojuego guardarVideojuego(Videojuego videojuego, Long proveedorId,
                                        List<Long> categoriaIds, List<Long> plataformaIds) {

        normalizarCampos(videojuego);
        validarObligatorios(videojuego);
        validarPrecios(videojuego);

        if (videojuego.getId() == null) {
            return crearVideojuego(videojuego, proveedorId, categoriaIds, plataformaIds);
        }
        return actualizarVideojuego(videojuego, proveedorId, categoriaIds, plataformaIds);
    }

    private Videojuego crearVideojuego(Videojuego videojuego, Long proveedorId,
                                       List<Long> categoriaIds, List<Long> plataformaIds) {

        videojuego.setProveedor(buscarProveedor(proveedorId));
        videojuego.setCategorias(buscarCategorias(categoriaIds));
        videojuego.setPlataformas(buscarPlataformas(plataformaIds));

        return videojuegoDao.save(videojuego);
    }

    private Videojuego actualizarVideojuego(Videojuego videojuego, Long proveedorId,
                                            List<Long> categoriaIds, List<Long> plataformaIds) {

        Videojuego existente = buscarVideojuegoXId(videojuego.getId());

        existente.setTitulo(videojuego.getTitulo());
        existente.setSinopsis(videojuego.getSinopsis());
        existente.setPrecio(videojuego.getPrecio());
        existente.setPrecioDescuento(videojuego.getPrecioDescuento());
        existente.setFechaLanzamiento(videojuego.getFechaLanzamiento());
        existente.setPegi(videojuego.getPegi());
        existente.setActivo(videojuego.isActivo());
        existente.setUmbralStockBajo(videojuego.getUmbralStockBajo());
        existente.setCategorias(buscarCategorias(categoriaIds));
        existente.setPlataformas(buscarPlataformas(plataformaIds));

        if (existente.getColaborador() == null) {
            existente.setProveedor(buscarProveedor(proveedorId));
        }
        return videojuegoDao.save(existente);
    }

    private void normalizarCampos(Videojuego videojuego) {

        if (videojuego.getTitulo() != null) {
            videojuego.setTitulo(videojuego.getTitulo().trim());
        }

        if (videojuego.getSinopsis() != null && videojuego.getSinopsis().isBlank()) {
            videojuego.setSinopsis(null);
        }
    }

    private void validarObligatorios(Videojuego videojuego) {

        if (videojuego.getTitulo() == null || videojuego.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio");
        }

        if (videojuego.getPrecio() == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }

        if (!PEGI_VALIDOS.contains(videojuego.getPegi())) {
            throw new IllegalArgumentException("La clasificacion PEGI no es valida");
        }

        if (videojuego.getUmbralStockBajo() < 0) {
            throw new IllegalArgumentException("El umbral de stock bajo no puede ser negativo");
        }
    }

    private void validarPrecios(Videojuego videojuego) {

        if (videojuego.getPrecio().signum() < 0) {
            throw new PrecioInvalidoException();
        }

        BigDecimal descuento = videojuego.getPrecioDescuento();

        if (descuento == null) {
            return;
        }

        if (descuento.signum() < 0 || descuento.compareTo(videojuego.getPrecio()) >= 0) {
            throw new PrecioDescuentoInvalidoException();
        }
    }

    private Proveedor buscarProveedor(Long proveedorId) {

        if (proveedorId == null) {
            throw new ProveedorObligatorioException();
        }

        Proveedor proveedor = proveedorDao.findById(proveedorId).orElse(null);

        if (proveedor == null) {
            throw new IllegalArgumentException("El proveedor no existe");
        }
        return proveedor;
    }

    private Set<Categoria> buscarCategorias(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        Set<Categoria> encontradas = new HashSet<>(categoriaDao.findAllById(ids));

        if (encontradas.size() != new HashSet<>(ids).size()) {
            throw new IllegalArgumentException("Alguna categoria no existe");
        }
        return encontradas;
    }

    private Set<Plataforma> buscarPlataformas(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        Set<Plataforma> encontradas = new HashSet<>(plataformaDao.findAllById(ids));

        if (encontradas.size() != new HashSet<>(ids).size()) {
            throw new IllegalArgumentException("Alguna plataforma no existe");
        }
        return encontradas;
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    @Override
    public void eliminarVideojuego(Long id) {

        Videojuego videojuego = buscarVideojuegoXId(id);

        try {
            videojuegoDao.delete(videojuego);
            videojuegoDao.flush();

        } catch (DataIntegrityViolationException e) {
            throw new VideojuegoConReferenciasException();
        }
    }

    @Override
    public List<Proveedor> listarProveedoresActivos() {
        return proveedorDao.listarFiltrado(null, null, true);
    }

    @Override
    public List<Categoria> listarCategorias() {
        return categoriaDao.listarFiltrado(null);
    }

    @Override
    public List<Plataforma> listarPlataformas() {
        return plataformaDao.listarFiltrado(null);
    }
}
