package model.aprovisionamiento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_comercial", nullable = false, length = 120)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String cif;

    @Column(length = 34)
    private String iban;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 60)
    private String pais;

    @Column(nullable = false)
    private boolean activo = true;
}
