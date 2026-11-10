package dao;

import java.util.List;
import model.inventario.ClaveLicencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaveLicenciaDao extends JpaRepository<ClaveLicencia, Long> {

    // La biblioteca de un cliente: sus claves VENDIDAS, y solo las suyas. El estado va
    // escrito en la consulta y no es un parametro, para que nadie pueda pedir una
    // biblioteca con claves ANULADAS o con las que todavia estan en stock. Los cuatro
    // fetch son lo que pinta la pantalla, que con open-in-view desactivado hay que traer
    // aqui; el distinct lo exige el fetch de plataformas, o un juego de dos saldria dos veces
    @Query("select distinct c from ClaveLicencia c "
            + "join fetch c.videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "left join fetch v.plataformas "
            + "where c.cliente.email = :email "
            + "and c.estado = model.inventario.EstadoClave.VENDIDA "
            + "order by c.fechaVenta desc, v.titulo")
    List<ClaveLicencia> listarBiblioteca(@Param("email") String email);

    // El guardia de la descarga de un juego de pago: devuelve si ese cliente tiene una
    // clave VENDIDA suya. Un count y no la clave entera, porque solo hace falta saber si
    // existe y asi no se descifra un codigo AES para tirarlo
    @Query("select count(c) > 0 from ClaveLicencia c "
            + "where c.cliente.email = :email "
            + "and c.videojuego.id = :videojuegoId "
            + "and c.estado = model.inventario.EstadoClave.VENDIDA")
    boolean estaEnBiblioteca(@Param("email") String email,
                             @Param("videojuegoId") Long videojuegoId);
}
