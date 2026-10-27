/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PC
 */
@Entity
@Table(name = "departament")
@Data
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 80)
    private String nom;

    private String descripcio;

    @OneToMany(mappedBy = "departamento")
    private List<Empleado> empleados = new ArrayList<>();

    public int nombreEmpleados() {
        return empleados.size();
    }
}
