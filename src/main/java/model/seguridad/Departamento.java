/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PC
 */
@Entity
@Table(name = "departamento")
@Getter
@Setter
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", unique = true)
    private Empleado responsable;

    @OneToMany(mappedBy = "departamento")
    private List<Empleado> empleados = new ArrayList<>();

    public int numeroEmpleados() {
        return empleados.size();
    }
}
