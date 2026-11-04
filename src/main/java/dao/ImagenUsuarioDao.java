package dao;

import model.seguridad.ImagenUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagenUsuarioDao extends JpaRepository<ImagenUsuario, Long> {
}
