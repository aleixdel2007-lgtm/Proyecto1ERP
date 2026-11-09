package dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import model.catalogo.Videojuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideojuegoDao extends JpaRepository<Videojuego, Long> {

    @Query("select distinct v from Videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "left join v.categorias c "
            + "left join v.plataformas p "
            + "where (:texto is null or lower(v.titulo) like lower(concat(:texto, '%'))) "
            + "and (:categoriaId is null or c.id = :categoriaId) "
            + "and (:plataformaId is null or p.id = :plataformaId) "
            + "and (:precioMin is null or v.precio >= :precioMin) "
            + "and (:precioMax is null or v.precio <= :precioMax) "
            + "and (:activo is null or v.activo = :activo) "
            + "order by v.titulo")
    List<Videojuego> listarFiltrado(@Param("texto") String texto,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("plataformaId") Long plataformaId,
                                    @Param("precioMin") BigDecimal precioMin,
                                    @Param("precioMax") BigDecimal precioMax,
                                    @Param("activo") Boolean activo);

    @Query("select distinct v from Videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "left join fetch v.categorias "
            + "left join fetch v.plataformas "
            + "where v.id in :ids "
            + "order by v.titulo")
    List<Videojuego> listarConClasificacion(@Param("ids") List<Long> ids);

    @Query("select distinct v from Videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "left join fetch v.categorias "
            + "left join fetch v.plataformas "
            + "where v.id = :id")
    Optional<Videojuego> buscarConDetalle(@Param("id") Long id);

    long countByColaboradorId(Long colaboradorId);

    @Query("select v.colaborador.id, count(v) from Videojuego v "
            + "where v.colaborador is not null "
            + "group by v.colaborador.id")
    List<Object[]> contarPorColaborador();

    long countByCategoriasId(Long categoriaId);

    @Query("select c.id, count(v) from Videojuego v "
            + "join v.categorias c "
            + "group by c.id")
    List<Object[]> contarPorCategoria();

    long countByPlataformasId(Long plataformaId);

    @Query("select p.id, count(v) from Videojuego v "
            + "join v.plataformas p "
            + "group by p.id")
    List<Object[]> contarPorPlataforma();

    /*
     * Listado de la tienda: solo juegos activos, saltandose cada filtro que llegue a null.
     * El activo va escrito en la consulta y no es un parametro, para que desde la tienda no
     * se pueda pedir un juego retirado ni por error. El precio se compara contra el efectivo
     * (el rebajado si lo hay), que es lo que el cliente va a pagar y por tanto lo que espera
     * filtrar. soloColaborador tiene tres estados: null los dos tipos, true solo los de
     * colaborador y false solo los de distribuidor. soloOfertas solo tiene dos, true los
     * rebajados y null o false todos, porque no hay ninguna pantalla que pida los que NO
     * tienen descuento. Las N:M se unen SIN fetch para que el filtro no duplique filas;
     * las colecciones las trae despues listarConClasificacion
     */
    @Query("select distinct v from Videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "left join v.categorias c "
            + "left join v.plataformas p "
            + "where v.activo = true "
            + "and (:texto is null or lower(v.titulo) like lower(concat('%', :texto, '%'))) "
            + "and (:categoriaId is null or c.id = :categoriaId) "
            + "and (:plataformaId is null or p.id = :plataformaId) "
            + "and (:precioMin is null or coalesce(v.precioDescuento, v.precio) >= :precioMin) "
            + "and (:precioMax is null or coalesce(v.precioDescuento, v.precio) <= :precioMax) "
            + "and (:soloColaborador is null "
            + "     or (:soloColaborador = true and v.colaborador is not null) "
            + "     or (:soloColaborador = false and v.colaborador is null)) "
            + "and (:soloOfertas is null or :soloOfertas = false "
            + "     or v.precioDescuento is not null) "
            + "order by v.titulo")
    List<Videojuego> listarCatalogo(@Param("texto") String texto,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("plataformaId") Long plataformaId,
                                    @Param("precioMin") BigDecimal precioMin,
                                    @Param("precioMax") BigDecimal precioMax,
                                    @Param("soloColaborador") Boolean soloColaborador,
                                    @Param("soloOfertas") Boolean soloOfertas);

    /*
     * La ficha que abre un cliente, con el proveedor, el colaborador y las dos N:M cargados.
     * No se usa buscarConDetalle porque esa no mira el activo, y un cliente que escriba el id
     * de un juego retirado en la URL veria su ficha. Al no encontrarlo devuelve el Optional
     * vacio y el servicio lanza VideojuegoNoEncontradoException, que ya responde un 404
     */
    @Query("select distinct v from Videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "left join fetch v.categorias "
            + "left join fetch v.plataformas "
            + "where v.id = :id and v.activo = true")
    Optional<Videojuego> buscarActivoConDetalle(@Param("id") Long id);
}
