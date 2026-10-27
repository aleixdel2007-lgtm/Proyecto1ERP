package model.ventas;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.seguridad.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE_PAGO;

    @Column(name = "base_imponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseImponible;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaPedido> lineas = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
