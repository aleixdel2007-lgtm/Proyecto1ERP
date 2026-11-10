package service.implement;

import config.AlmacenArchivos;
import dao.ClaveLicenciaDao;
import dao.ImagenVideojuegoDao;
import dao.VersionJuegoDao;
import dao.VideojuegoDao;
import exception.biblioteca.DescargaNoDisponibleException;
import exception.biblioteca.JuegoNoAdquiridoException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.publicacion.VersionJuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.BibliotecaService;

@Service
@Transactional(readOnly = true)
public class BibliotecaServiceImpl implements BibliotecaService {

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private VersionJuegoDao versionJuegoDao;

    @Autowired
    private ImagenVideojuegoDao imagenVideojuegoDao;

    @Autowired
    private AlmacenArchivos almacenArchivos;

    @Override
    public List<ClaveLicencia> listarBiblioteca(String email) {
        return claveLicenciaDao.listarBiblioteca(email);
    }

    @Override
    public Map<Long, String> listarPortadas(List<ClaveLicencia> claves) {

        Map<Long, String> portadas = new HashMap<>();

        if (claves == null || claves.isEmpty()) {
            return portadas;
        }

        for (Object[] fila : imagenVideojuegoDao.buscarPorTipo(idsDe(claves), TipoImagen.PORTADA)) {
            portadas.putIfAbsent((Long) fila[0], (String) fila[1]);
        }
        return portadas;
    }

    @Override
    public Map<Long, VersionJuego> listarVersionesActivas(List<ClaveLicencia> claves) {

        Map<Long, VersionJuego> versiones = new HashMap<>();

        if (claves == null || claves.isEmpty()) {
            return versiones;
        }

        for (VersionJuego version : versionJuegoDao.buscarActivas(idsDe(claves))) {
            versiones.put(version.getVideojuego().getId(), version);
        }
        return versiones;
    }

    @Override
    public ArchivoDescarga prepararDescarga(String email, Long videojuegoId) {

        Videojuego videojuego = videojuegoDao.findById(videojuegoId)
                .orElseThrow(() -> new VideojuegoNoEncontradoException(videojuegoId));

        boolean gratis = precioEfectivo(videojuego).signum() == 0;

        // Un juego gratis se baja sin clave y sin pasar por la biblioteca, pero solo
        // mientras siga publicado: retirado no lo tiene nadie, asi que no hay a quien
        // servirselo y se responde lo mismo que en su ficha, que no existe
        if (gratis && !videojuego.isActivo()) {
            throw new VideojuegoNoEncontradoException(videojuegoId);
        }

        // Y uno de pago, solo si tienes una clave VENDIDA suya. Aqui no se mira el activo:
        // lo compraste, y que se retire de la tienda no te lo quita
        if (!gratis && !claveLicenciaDao.estaEnBiblioteca(email, videojuegoId)) {
            throw new JuegoNoAdquiridoException(videojuegoId);
        }

        VersionJuego version = versionJuegoDao.findByVideojuegoIdAndActivaTrue(videojuegoId)
                .orElseThrow(DescargaNoDisponibleException::new);

        return new ArchivoDescarga(almacenArchivos.leerZip(version.getRutaArchivo()),
                version.getNombreArchivo(), version.getTamanoBytes());
    }

    // Los ids de los VIDEOJUEGOS de una lista de claves, no los ids de las claves. Es lo
    // que usan de clave los dos mapas de arriba, porque la pantalla busca la portada y la
    // version por el juego y no por la clave con la que lo compraste
    private List<Long> idsDe(List<ClaveLicencia> claves) {

        List<Long> ids = new ArrayList<>();

        for (ClaveLicencia clave : claves) {
            ids.add(clave.getVideojuego().getId());
        }
        return ids;
    }

    private BigDecimal precioEfectivo(Videojuego videojuego) {

        return videojuego.getPrecioDescuento() != null
                ? videojuego.getPrecioDescuento() : videojuego.getPrecio();
    }
}
