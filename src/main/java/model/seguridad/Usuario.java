/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
/**
 *
 * @author PC
 */
@Entity
@Table(name = "usuari")
@Data
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String nom;

    @Column(length = 120)
    private String cognoms;

    @Column(nullable = false)
    private boolean actiu = true;

    @Column(name = "data_alta", nullable = false)
    private LocalDateTime dataAlta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

}
