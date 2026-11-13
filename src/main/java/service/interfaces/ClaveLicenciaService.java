package service.interfaces;

import java.util.List;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;

public interface ClaveLicenciaService {

    // Las claves del panel, saltandose cada filtro que llegue vacio
    List<ClaveLicencia> listarClaves(String codigo, Long videojuegoId, EstadoClave estado);

    // Cuantas claves hay en un estado, para los contadores de la cabecera
    long contarPorEstado(EstadoClave estado);

    // Los videojuegos que rellenan el desplegable del filtro
    List<Videojuego> listarVideojuegos();

    // Una clave con su juego, su cliente y su compra ya cargados
    ClaveLicencia buscarClaveXId(Long id);

    // Deja una clave DISPONIBLE inservible para siempre, con su fecha y su motivo
    void anularClave(Long id, String motivo);

    // Mira cuantas claves quedan de un juego y, si son pocas, avisa por correo a los gestores de inventario
    void avisarSiQuedaPocoStock(Videojuego videojuego);
}
