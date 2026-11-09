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


    @Query("select distinct v from Videojuego v "
            + "left join fetch v.categorias "
            + "left join fetch v.plataformas "
            + "where v.colaborador.email = :email "
            + "order by v.titulo")
    List<Videojuego> listarDeColaborador(@Param("email") String email);

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
}
