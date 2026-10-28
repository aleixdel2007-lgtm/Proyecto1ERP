package model.aprovisionamiento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.catalogo.Videojuego;

import java.math.BigDecimal;

@Entity
@Table(name = "linea_compra")
@Getter
@Setter
public class LineaCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "cantidad_recibida")
    private Integer cantidadRecibida;

    @Column(name = "coste_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal costeUnitario = BigDecimal.ZERO;
}
