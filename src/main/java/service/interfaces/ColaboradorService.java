package service.interfaces;

import java.util.List;
import java.util.Map;
import model.seguridad.Colaborador;

public interface ColaboradorService {

    List<Colaborador> listarColaboradores(String texto, Boolean activo);

    Colaborador buscarColaboradorXId(Long id);

    Colaborador guardarColaborador(Colaborador colaborador, String password);

    void eliminarColaborador(Long id);

    Map<Long, Long> contarJuegos();

    long contarJuegosXColaborador(Long id);
}
