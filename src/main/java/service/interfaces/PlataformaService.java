package service.interfaces;

import java.util.List;
import java.util.Map;
import model.catalogo.Plataforma;

public interface PlataformaService {

    List<Plataforma> listarPlataformas(String texto);

    Plataforma buscarPlataformaXId(Long id);

    Plataforma guardarPlataforma(Plataforma plataforma);

    void eliminarPlataforma(Long id);

    Map<Long, Long> contarJuegos();

    long contarJuegosXPlataforma(Long id);
}
