package model.aprovisionamiento;

import config.CifradoAesConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.seguridad.CuentaProveedor;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoProveedor tipo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", unique = true)
    private CuentaProveedor cuenta;

    @Convert(converter = CifradoAesConverter.class)
    @Column(name = "cif", nullable = false, length = 128)
    private String cif;

    @Column(name = "cif_hash", nullable = false, unique = true, length = 64)
    private String cifHash;

    @Convert(converter = CifradoAesConverter.class)
    @Column(name = "iban", length = 128)
    private String iban;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "pais", length = 60)
    private String pais;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
