/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.Optional;
import model.ventas.Cesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Sandy
 */
public interface CestaDao extends JpaRepository<Cesta, Long> {

    // busca la cesta del cliente con este email; vacío si todavía no tiene
    Optional<Cesta> findByClienteEmail(String email);

    //para traer la cesta entera del cliente
    @Query("select distinct c from Cesta c "
            + "left join fetch c.lineas l "
            + "left join fetch l.videojuego "
            + "where c.cliente.email = :email")
    Optional<Cesta> buscarConLineasXEmail(@Param("email") String email);
}
