package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import model.seguridad.Departamento;
import model.seguridad.Usuario;

import java.time.LocalDate;
/**
 *
 * @author PC
 */
@Entity
@Table(name = "perfil_empleat")
public class Empleado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuari_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departament_id", nullable = false)
    private Departamento departamento;

    @Column(length = 20)
    private String nif;

    @Column(length = 34)
    private String iban;

    @Column(name = "data_contractacio")
    private LocalDate dataContractacio;

    @Column(length = 80)
    private String carrec;
}
