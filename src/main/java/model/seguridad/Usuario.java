/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author PC
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private Rol rol = Rol.CLIENTE;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 120)
    private String apellidos;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

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

    @PrePersist
    private void alCrear() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}
