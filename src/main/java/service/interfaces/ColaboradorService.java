package service.interfaces;

import java.util.List;
import java.util.Map;
import model.seguridad.Colaborador;
import model.seguridad.ImagenUsuario;
import org.springframework.web.multipart.MultipartFile;

public interface ColaboradorService {

    List<Colaborador> listarColaboradores(String texto, Boolean activo);

    Colaborador buscarColaboradorXId(Long id);

    Colaborador guardarColaborador(Colaborador colaborador, String password);

    void eliminarColaborador(Long id);

    ImagenUsuario buscarImagenPerfil(Long colaboradorId);

    Map<Long, String> listarImagenesPerfil(List<Colaborador> colaboradores);

    void guardarImagenPerfil(Long colaboradorId, MultipartFile archivo);

    Map<Long, Long> contarJuegos();

    long contarJuegosXColaborador(Long id);
}
