/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.Optional;
import model.ventas.Cesta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Sandy
 */
public interface CestaDao extends JpaRepository<Cesta, Long> {

    // busca la cesta del cliente con este email; vacío si todavía no tiene
    Optional<Cesta> findByClienteEmail(String email);

}
