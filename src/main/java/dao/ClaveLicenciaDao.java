package dao;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // Hay ya una clave con ese hash? Lo pregunta el acunado antes de dar un codigo por bueno,
    // que codigo_hash es UNIQUE y un choque reventaria el insert entero
    boolean existsByCodigoHash(String codigoHash);

    // La primera clave por id disponible de un juego. El bloqueo hace que, si dos clientes pagan
    // a la vez, el segundo espere y no se le entregue la misma clave
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ClaveLicencia> findFirstByVideojuegoIdAndEstadoOrderByIdAsc(
            Long videojuegoId, EstadoClave estado);

    // Las claves de un pedido que estan en un estado. Sirve para encontrar "las claves
    // RESERVADAS de este pedido": al pagar pasan a VENDIDA y al caducar vuelven a DISPONIBLE
    List<ClaveLicencia> findByLineaPedidoPedidoIdAndEstado(Long pedidoId, EstadoClave estado);
    
    
    // El listado del panel. Los tres filtros son opcionales y se saltan solos cuando llegan
    // a null, igual que en los demas listarFiltrado del proyecto. Trae con fetch el juego,
    // el cliente y la orden de compra porque la tabla pinta los tres y open-in-view esta
    // desactivado. Sin distinct a proposito: aqui no se une ninguna coleccion, asi que cada
    // clave sale una sola vez. El codigo se busca por su hash, nunca por la columna cifrada:
    // AES-GCM no es determinista y un like contra ella no encontraria nada
    @Query("select c from ClaveLicencia c "
            + "join fetch c.videojuego v "
            + "left join fetch c.cliente "
            + "left join fetch c.lineaCompra lc "
            + "left join fetch lc.compra "
            + "where (:codigoHash is null or c.codigoHash = :codigoHash) "
            + "and (:videojuegoId is null or v.id = :videojuegoId) "
            + "and (:estado is null or c.estado = :estado) "
            + "order by c.fechaAlta desc, c.id desc")
    List<ClaveLicencia> listarFiltrado(@Param("codigoHash") String codigoHash,
            @Param("videojuegoId") Long videojuegoId,
            @Param("estado") EstadoClave estado);

    // Los cuatro contadores de la cabecera, uno por estado
    @Query("select count(c) from ClaveLicencia c where c.estado = :estado")
    long contarPorEstado(@Param("estado") EstadoClave estado);

    // La clave sola, con lo que pinta la pantalla de anular ya cargado. No vale un findById:
    // con open-in-view desactivado, el juego y la compra llegarian como proxys y la
    // transaccion ya estaria cerrada cuando la plantilla los lee
    @Query("select c from ClaveLicencia c "
            + "join fetch c.videojuego "
            + "left join fetch c.cliente "
            + "left join fetch c.lineaCompra lc "
            + "left join fetch lc.compra "
            + "where c.id = :id")
    Optional<ClaveLicencia> buscarConDetalle(@Param("id") Long id);

    // Cuantas claves quedan en un estado para UN juego. Con DISPONIBLE es el stock de ese juego
    @Query("select count(c) from ClaveLicencia c "
            + "where c.videojuego.id = :videojuegoId "
            + "and c.estado = :estado")
    long contarPorVideojuegoYEstado(@Param("videojuegoId") Long videojuegoId,
            @Param("estado") EstadoClave estado);

    @Query("select sum(lc.costeUnitario) from ClaveLicencia c "
            + "join c.lineaCompra lc "
            + "where c.estado = :estado")
    BigDecimal sumarCostePorEstado(@Param("estado") EstadoClave estado);

    @Query("select count(distinct c.videojuego.id) from ClaveLicencia c where c.estado = :estado")
    long contarJuegosConClavesEn(@Param("estado") EstadoClave estado);
}
