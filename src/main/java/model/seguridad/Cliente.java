package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cliente")
@Getter
@Setter
public class Cliente extends Usuario {

    public Cliente() {
        setRol(Rol.CLIENTE);
    }

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 160)
    private String direccion;

    @Column(name = "ciudad", length = 80)
    private String ciudad;

    @Column(name = "pais", length = 60)
    private String pais;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
}
