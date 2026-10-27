package model.ventas;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;

import java.math.BigDecimal;

@Entity
@Table(name = "linea_pedido")
@Getter
@Setter
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clave_licencia_id", nullable = false)
    private ClaveLicencia claveLicencia;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;
}
