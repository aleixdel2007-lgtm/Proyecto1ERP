package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private Rol rol;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "apellidos", length = 120)
    private String apellidos;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "debe_cambiar_password", nullable = false)
    private boolean debeCambiarPassword = false;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @PrePersist
    private void alCrear() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}
