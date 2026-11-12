package service.implement;

import config.AlmacenArchivos;
import dao.CategoriaDao;
import dao.ColaboradorDao;
import model.seguridad.Colaborador;
import org.springframework.transaction.annotation.Transactional;
import dao.ImagenVideojuegoDao;
import dao.PlataformaDao;
import dao.VersionJuegoDao;
import model.publicacion.VersionJuego;
import dao.ProveedorDao;
import dao.VideojuegoDao;
import exception.imagen.ImagenInvalidaException;
import exception.imagen.ImagenNoEncontradaException;
import exception.precio.PrecioDescuentoInvalidoException;
import exception.precio.PrecioInvalidoException;
import exception.videojuego.ProveedorObligatorioException;
import exception.videojuego.VideojuegoConReferenciasException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Categoria;
import model.catalogo.ImagenVideojuego;
import model.catalogo.Plataforma;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import service.interfaces.VideojuegoService;

@Service
public class VideojuegoServiceImpl implements VideojuegoService {

    private static final List<Integer> PEGI_VALIDOS = List.of(3, 7, 12, 16, 18);

    private static final int MAXIMO_CAPTURAS = 4;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private ImagenVideojuegoDao imagenVideojuegoDao;

    @Autowired
    private VersionJuegoDao versionJuegoDao;

    @Autowired
    private AlmacenArchivos almacenArchivos;

    @Autowired
    private ProveedorDao proveedorDao;

    @Autowired
    private ColaboradorDao colaboradorDao;

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
    public List<Videojuego> listarJuegosDeColaborador(String email) {
        return videojuegoDao.listarDeColaborador(email);
    }

    @Override
    public Map<Long, VersionJuego> listarVersionesActivas(List<Videojuego> videojuegos) {

        Map<Long, VersionJuego> versiones = new HashMap<>();

        if (videojuegos == null || videojuegos.isEmpty()) {
            return versiones;
        }

        List<Long> ids = new ArrayList<>();

        for (Videojuego videojuego : videojuegos) {
            ids.add(videojuego.getId());
        }

        for (VersionJuego version : versionJuegoDao.buscarActivas(ids)) {
            versiones.put(version.getVideojuego().getId(), version);
        }
        return versiones;
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

        almacenArchivos.borrarCarpeta(id);
    }

    @Override
    public List<ImagenVideojuego> listarImagenes(Long videojuegoId) {
        return imagenVideojuegoDao.findByVideojuegoIdOrderByOrdenAscIdAsc(videojuegoId);
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
    public ImagenVideojuego guardarImagen(Long videojuegoId, MultipartFile archivo,
                                          TipoImagen tipo, String textoAlternativo) {

        Videojuego videojuego = buscarVideojuegoXId(videojuegoId);

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de imagen es obligatorio");
        }

        String extension = almacenArchivos.extensionDe(archivo);

        if (tipo != TipoImagen.CAPTURA) {
            validarQueNoExista(videojuegoId, tipo);
        }

        String ruta = almacenArchivos.guardarImagen(
                videojuegoId, videojuego.getTitulo(), archivo, extension);

        ImagenVideojuego imagen = new ImagenVideojuego();
        imagen.setVideojuego(videojuego);
        imagen.setTipo(tipo);
        imagen.setRuta(ruta);
        imagen.setOrden(imagenVideojuegoDao.maximoOrden(videojuegoId, tipo) + 1);
        imagen.setTextoAlternativo(textoAlternativoDe(textoAlternativo, videojuego));

        try {
            return imagenVideojuegoDao.save(imagen);

        } catch (RuntimeException e) {
            almacenArchivos.borrar(ruta);
            throw e;
        }
    }

    @Override
    public void eliminarImagen(Long videojuegoId, Long imagenId) {

        ImagenVideojuego imagen = buscarImagenXId(videojuegoId, imagenId);
        String ruta = imagen.getRuta();

        imagenVideojuegoDao.delete(imagen);
        imagenVideojuegoDao.flush();

        almacenArchivos.borrar(ruta);
    }

    private void validarQueNoExista(Long videojuegoId, TipoImagen tipo) {

        if (!imagenVideojuegoDao.findByVideojuegoIdAndTipoOrderByOrdenAscIdAsc(videojuegoId, tipo).isEmpty()) {

            throw new ImagenInvalidaException("Ya hay una imagen de tipo " + tipo
                    + ": quitala antes de subir otra");
        }
    }

    private ImagenVideojuego buscarImagenXId(Long videojuegoId, Long imagenId) {

        ImagenVideojuego imagen = imagenVideojuegoDao.findById(imagenId).orElse(null);

        if (imagen == null || !imagen.getVideojuego().getId().equals(videojuegoId)) {
            throw new ImagenNoEncontradaException(imagenId);
        }
        return imagen;
    }

    private String textoAlternativoDe(String textoAlternativo, Videojuego videojuego) {

        if (textoAlternativo == null || textoAlternativo.isBlank()) {
            return videojuego.getTitulo();
        }
        return textoAlternativo.trim();
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

    @Override
    public void validarFichaColaborador(String titulo, String sinopsis, String pegi, String version,
                                        List<Long> categoriaIds, List<Long> plataformaIds) {

        validarFicha(titulo, sinopsis, pegi, categoriaIds, plataformaIds);
        validarVersion(version);
    }

    private void validarFicha(String titulo, String sinopsis, String pegi,
                              List<Long> categoriaIds, List<Long> plataformaIds) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio");
        }

        if (titulo.trim().length() > 150) {
            throw new IllegalArgumentException("El titulo no puede pasar de 150 caracteres");
        }

        if (sinopsis == null || sinopsis.isBlank()) {
            throw new IllegalArgumentException("La descripcion es obligatoria");
        }

        if (pegi == null || !listarPegis().contains(pegi)) {
            throw new IllegalArgumentException("La clasificacion PEGI no es valida");
        }

        if (categoriaIds == null || categoriaIds.isEmpty()) {
            throw new IllegalArgumentException("Elige al menos una categoria");
        }

        if (plataformaIds == null || plataformaIds.isEmpty()) {
            throw new IllegalArgumentException("Elige al menos una plataforma");
        }

        // Comprueba que los ids existen: lanzan "Alguna categoria/plataforma no existe"
        buscarCategorias(categoriaIds);
        buscarPlataformas(plataformaIds);
    }

    // Letras, numeros, puntos, + y guiones; maximo 20, que es lo que cabe en la columna
    private void validarVersion(String version) {

        if (version == null || !version.trim().matches("[0-9A-Za-z][0-9A-Za-z.+-]{0,19}")) {
            throw new IllegalArgumentException(
                    "La version no es valida: letras, numeros, puntos y guiones, maximo 20 (por ejemplo 1.0.0)");
        }
    }

    @Override
    @Transactional
    public Videojuego publicarJuegoColaborador(String emailColaborador, String titulo, String sinopsis,
                                               String pegi, String version,
                                               List<Long> categoriaIds, List<Long> plataformaIds,
                                               MultipartFile zip, MultipartFile portada,
                                               MultipartFile banner, List<MultipartFile> capturas) {

        // Todo lo que puede fallar por culpa del usuario, antes de escribir nada
        validarFichaColaborador(titulo, sinopsis, pegi, version, categoriaIds, plataformaIds);
        almacenArchivos.validarZip(zip);
        almacenArchivos.extensionDe(portada);

        List<MultipartFile> nuevasCapturas = limpiarCapturas(capturas);

        validarImagenesDeColaborador(null, null, banner, nuevasCapturas);

        Colaborador colaborador = colaboradorDao.findByEmail(emailColaborador).orElse(null);

        if (colaborador == null) {
            throw new IllegalArgumentException("Tu cuenta no es de colaborador");
        }

        Videojuego videojuego = new Videojuego();
        videojuego.setTitulo(titulo.trim());
        videojuego.setSinopsis(sinopsis.trim());
        videojuego.setPegi(Integer.parseInt(pegi));
        videojuego.setColaborador(colaborador);
        videojuego.setCategorias(buscarCategorias(categoriaIds));
        videojuego.setPlataformas(buscarPlataformas(plataformaIds));
        // Siempre se crea inactivo esperando a la confirmacion del admin o gestor de videojuegos.
        videojuego.setActivo(false);
        // Se pone el precio a 0 ya que un juego de un colaborador sera siempre gratis.
        videojuego.setPrecio(BigDecimal.ZERO);
        // Es un archivo descargable: no hay unidades que se agoten
        videojuego.setStockIlimitado(true);

        Videojuego guardado = videojuegoDao.save(videojuego);

        try {
            guardarImagenesDeColaborador(guardado.getId(), portada, banner, nuevasCapturas);

            AlmacenArchivos.ZipGuardado archivo = almacenArchivos.guardarZip(guardado.getId(), zip);

            VersionJuego nueva = new VersionJuego();
            nueva.setVideojuego(guardado);
            nueva.setNumero(version.trim());
            nueva.setNombreArchivo(archivo.nombreArchivo());
            nueva.setRutaArchivo(archivo.ruta());
            nueva.setTamanoBytes(archivo.tamanoBytes());
            nueva.setSha256(archivo.sha256());
            nueva.setActiva(true);
            versionJuegoDao.save(nueva);

        } catch (RuntimeException e) {
            // La base de datos se deshace sola al propagarse la excepcion; los archivos no
            almacenArchivos.borrarCarpeta(guardado.getId());
            almacenArchivos.borrarZips(guardado.getId());
            throw e;
        }
        return guardado;
    }

    @Override
    public List<String> listarPegis() {
        List<String> pegis = new ArrayList<>();

        for (Integer pegi : PEGI_VALIDOS) {
            pegis.add(String.valueOf(pegi));
        }
        return pegis;
    }

    @Override
    @Transactional(readOnly = true)
    public Videojuego buscarJuegoDeColaborador(String emailColaborador, Long videojuegoId) {

        Videojuego videojuego = videojuegoDao.buscarConDetalle(videojuegoId).orElse(null);

        if (videojuego == null || videojuego.getColaborador() == null
                || !videojuego.getColaborador().getEmail().equalsIgnoreCase(emailColaborador)) {

            throw new VideojuegoNoEncontradoException(videojuegoId);
        }
        return videojuego;
    }

    @Override
    public VersionJuego buscarVersionActiva(Long videojuegoId) {
        return versionJuegoDao.findByVideojuegoIdAndActivaTrue(videojuegoId).orElse(null);
    }

    @Override
    @Transactional
    public Videojuego actualizarJuegoColaborador(String emailColaborador, Long videojuegoId, String titulo,
                                                 String sinopsis, String pegi, String version,
                                                 List<Long> categoriaIds, List<Long> plataformaIds,
                                                 MultipartFile zip, MultipartFile portada,
                                                 MultipartFile banner, List<MultipartFile> capturas) {

        Videojuego videojuego = buscarJuegoDeColaborador(emailColaborador, videojuegoId);

        validarFicha(titulo, sinopsis, pegi, categoriaIds, plataformaIds);

        boolean archivoNuevo = !vacio(zip);

        if (archivoNuevo) {
            validarVersion(version);
            almacenArchivos.validarZip(zip);

            if (versionJuegoDao.existsByVideojuegoIdAndNumero(videojuegoId, version.trim())) {
                throw new IllegalArgumentException(
                        "Ya has subido una version con el numero " + version.trim());
            }
        }

        List<MultipartFile> nuevasCapturas = limpiarCapturas(capturas);

        validarImagenesDeColaborador(videojuegoId, portada, banner, nuevasCapturas);

        videojuego.setTitulo(titulo.trim());
        videojuego.setSinopsis(sinopsis.trim());
        videojuego.setPegi(Integer.parseInt(pegi));
        videojuego.setCategorias(buscarCategorias(categoriaIds));
        videojuego.setPlataformas(buscarPlataformas(plataformaIds));
        videojuego.setActivo(false);

        Videojuego guardado = videojuegoDao.save(videojuego);

        List<String> escritas = new ArrayList<>();
        String zipEscrito = null;

        try {
            escritas = guardarImagenesDeColaborador(videojuegoId, portada, banner, nuevasCapturas);

            if (archivoNuevo) {
                VersionJuego anterior = versionJuegoDao
                        .findByVideojuegoIdAndActivaTrue(videojuegoId).orElse(null);

                if (anterior != null) {
                    anterior.setActiva(false);
                    versionJuegoDao.save(anterior);
                }

                AlmacenArchivos.ZipGuardado archivo = almacenArchivos.guardarZip(videojuegoId, zip);
                zipEscrito = archivo.ruta();

                VersionJuego nueva = new VersionJuego();
                nueva.setVideojuego(guardado);
                nueva.setNumero(version.trim());
                nueva.setNombreArchivo(archivo.nombreArchivo());
                nueva.setRutaArchivo(archivo.ruta());
                nueva.setTamanoBytes(archivo.tamanoBytes());
                nueva.setSha256(archivo.sha256());
                nueva.setActiva(true);
                versionJuegoDao.save(nueva);
            }
            return guardado;

        } catch (RuntimeException e) {

            for (String ruta : escritas) {
                almacenArchivos.borrar(ruta);
            }

            if (zipEscrito != null) {
                almacenArchivos.borrarZip(zipEscrito);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void eliminarImagenDeColaborador(String emailColaborador, Long videojuegoId, Long imagenId) {

        buscarJuegoDeColaborador(emailColaborador, videojuegoId);
        eliminarImagen(videojuegoId, imagenId);
    }

    private boolean vacio(MultipartFile archivo) {
        return archivo == null || archivo.isEmpty();
    }

    private List<MultipartFile> limpiarCapturas(List<MultipartFile> capturas) {

        List<MultipartFile> utiles = new ArrayList<>();

        if (capturas == null) {
            return utiles;
        }

        for (MultipartFile captura : capturas) {

            if (!vacio(captura)) {
                utiles.add(captura);
            }
        }
        return utiles;
    }

    private void validarImagenesDeColaborador(Long videojuegoId, MultipartFile portada,
                                              MultipartFile banner, List<MultipartFile> capturas) {

        if (!vacio(portada)) {
            almacenArchivos.extensionDe(portada);

            if (videojuegoId != null) {
                validarQueNoExista(videojuegoId, TipoImagen.PORTADA);
            }
        }

        if (!vacio(banner)) {
            almacenArchivos.extensionDe(banner);

            if (videojuegoId != null) {
                validarQueNoExista(videojuegoId, TipoImagen.BANNER);
            }
        }

        int guardadas = videojuegoId == null ? 0
                : imagenVideojuegoDao
                        .findByVideojuegoIdAndTipoOrderByOrdenAscIdAsc(videojuegoId, TipoImagen.CAPTURA)
                        .size();

        if (guardadas + capturas.size() > MAXIMO_CAPTURAS) {
            throw new ImagenInvalidaException(
                    "Un juego no puede tener mas de " + MAXIMO_CAPTURAS + " capturas");
        }

        for (MultipartFile captura : capturas) {
            almacenArchivos.extensionDe(captura);
        }
    }

    private List<String> guardarImagenesDeColaborador(Long videojuegoId, MultipartFile portada,
                                                      MultipartFile banner, List<MultipartFile> capturas) {

        List<String> rutas = new ArrayList<>();

        if (!vacio(portada)) {
            rutas.add(guardarImagen(videojuegoId, portada, TipoImagen.PORTADA, null).getRuta());
        }

        if (!vacio(banner)) {
            rutas.add(guardarImagen(videojuegoId, banner, TipoImagen.BANNER, null).getRuta());
        }

        for (MultipartFile captura : capturas) {
            rutas.add(guardarImagen(videojuegoId, captura, TipoImagen.CAPTURA, null).getRuta());
        }
        return rutas;
    }
}
