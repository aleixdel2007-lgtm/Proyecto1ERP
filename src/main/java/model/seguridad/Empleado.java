package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 *
 * @author PC
 */
@Entity
@Table(name = "empleado")
@Getter
@Setter
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @Column(name = "nif", length = 20, unique = true)
    private String nif;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;
}
