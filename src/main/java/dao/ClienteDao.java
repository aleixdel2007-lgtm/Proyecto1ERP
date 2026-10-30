/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

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

    @Query("select c from Cliente c "
            + "where c.nombre like concat('%', :texto, '%') "
            + "or c.apellidos like concat('%', :texto, '%') "
            + "or c.email like concat('%', :texto, '%')")
    List<Cliente> buscarFiltrado(@Param("texto") String texto);

}
