package model.seguridad;

import config.CifradoAesConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "trabajador")
@Getter
@Setter
public class Trabajador extends Usuario {

    //cada trabajador pertenece a un departamento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @Column(name = "cargo", length = 80)
    private String cargo;

    @Convert(converter = CifradoAesConverter.class)
    @Column(name = "nif", length = 128)
    private String nif;

    @Column(name = "nif_hash", length = 64, unique = true)
    private String nifHash;

    @Convert(converter = CifradoAesConverter.class)
    @Column(name = "iban", length = 128) 
    private String iban;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;
}
