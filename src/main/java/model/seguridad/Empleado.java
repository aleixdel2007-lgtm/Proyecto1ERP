package model.seguridad;

import config.CifradoAesConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Getter
@Setter
public class Empleado extends Usuario {

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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaContratacion;
}
