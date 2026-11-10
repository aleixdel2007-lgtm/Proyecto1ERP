package service.interfaces;

import java.util.List;
import model.valoracion.Valoracion;

public interface ValoracionService {

    List<Valoracion> listarVisiblesPorVideojuego(Long videojuegoId);

    List<Valoracion> listarPorCliente(Long clienteId);

    Valoracion crear(String emailCliente, Long videojuegoId, String titulo, String descripcion, int puntuacion);

    boolean haValorado(String emailCliente, Long videojuegoId);
}
