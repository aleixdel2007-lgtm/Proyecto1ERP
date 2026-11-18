package dao;

import model.seguridad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioDao extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Suma uno al contador dentro de la propia sentencia, no leyendo y volviendo a guardar: dos
    // intentos a la vez leerian el mismo valor y al guardar se perderia uno. Devuelve las filas
    // tocadas, o sea 0 si ese correo no existe. clearAutomatically porque el servicio vuelve a
    // leer el usuario justo despues y el contexto JPA le daria el contador viejo
    @Modifying(clearAutomatically = true)
    @Query("update Usuario u set u.intentosFallidos = u.intentosFallidos + 1 where u.email = :email")
    int sumarIntentoFallido(@Param("email") String email);

    // Devuelve el contador a cero cuando se entra bien. El mayor que cero evita escribir en la
    // tabla en cada inicio de sesion correcto, que es la inmensa mayoria
    @Modifying(clearAutomatically = true)
    @Query("update Usuario u set u.intentosFallidos = 0 where u.email = :email and u.intentosFallidos > 0")
    int reiniciarIntentosFallidos(@Param("email") String email);
}
