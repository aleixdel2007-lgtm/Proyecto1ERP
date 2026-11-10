package service.interfaces;

import java.util.List;
import java.util.Map;
import model.inventario.ClaveLicencia;
import model.publicacion.VersionJuego;
import org.springframework.core.io.Resource;

public interface BibliotecaService {

    record ArchivoDescarga(Resource recurso, String nombreArchivo, long tamanoBytes) {
    }

    List<ClaveLicencia> listarBiblioteca(String email);

    Map<Long, String> listarPortadas(List<ClaveLicencia> claves);

    Map<Long, VersionJuego> listarVersionesActivas(List<ClaveLicencia> claves);

    ArchivoDescarga prepararDescarga(String email, Long videojuegoId);
}
