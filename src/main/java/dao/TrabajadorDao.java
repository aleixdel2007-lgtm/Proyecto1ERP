/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import model.seguridad.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Sandy
 */
public interface TrabajadorDao extends JpaRepository<Trabajador, Long> {

}
