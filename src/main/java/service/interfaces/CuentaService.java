package service.interfaces;

import java.time.LocalDate;

public interface CuentaService {

    void registrarCliente(String nombre, String apellidos, String email,
                          String password, String repetirPassword, String pais,
                          LocalDate fechaNacimiento, boolean aceptaCondiciones);

    void cambiarPassword(String email, String passwordActual,
                         String nuevaPassword, String repetirPassword);
}
