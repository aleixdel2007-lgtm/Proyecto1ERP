/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.time.LocalDateTime;
import java.util.List;
import model.seguridad.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Sandy
 */
public interface ClienteDao extends JpaRepository<Cliente, Long> {

    //Cada bloque (:algo is null or ...) significa: si ese filtro viene vacío la condición se cumple siempre y no filtra
    //si tiene valor si se aplcia 
    @Query("select c from Cliente c "
            + "where (:texto is null or c.nombre like concat('%', :texto, '%') "
            + "or c.apellidos like concat('%', :texto, '%') "
            + "or c.email like concat('%', :texto, '%')) "
            + "and (:pais is null or c.pais = :pais) "
            + "and (:desde is null or c.fechaAlta >= :desde)")
    List<Cliente> buscarFiltrado(@Param("texto") String texto,
            @Param("pais") String pais,
            @Param("desde") LocalDateTime desde);

    @Query("select distinct c.pais from Cliente c where c.pais is not null order by c.pais")
    List<String> listarPaises();
}
