package dao;

import model.seguridad.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrabajadorDao extends JpaRepository<Trabajador, Long> {

}
