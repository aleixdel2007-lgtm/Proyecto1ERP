package dao;

import java.util.List;
import model.catalogo.Videojuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VideojuegoDao extends JpaRepository<Videojuego, Long> {

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
}
