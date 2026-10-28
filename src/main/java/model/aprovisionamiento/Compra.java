package model.aprovisionamiento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.seguridad.Trabajador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
@Getter
@Setter
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_recepcion")
    private LocalDate fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.BORRADOR;

    @Column(name = "total_coste", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCoste = BigDecimal.ZERO;

    @Column(name = "numero_albaran", length = 40)
    private String numeroAlbaran;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaCompra> lineas = new ArrayList<>();
}
