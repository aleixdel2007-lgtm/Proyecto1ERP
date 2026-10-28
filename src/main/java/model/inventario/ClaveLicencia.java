package model.inventario;

import config.CifradoAesConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.aprovisionamiento.LiniaCompra;
import model.catalogo.Videojuego;
import model.seguridad.Cliente;
import model.ventas.LineaPedido;

import java.time.LocalDateTime;

@Entity
@Table(name = "clave_licencia")
@Getter
@Setter
public class ClaveLicencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linea_compra_id", nullable = false)
    private LiniaCompra lineaCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linea_pedido_id", unique = true)
    private LineaPedido lineaPedido;

    @Convert(converter = CifradoAesConverter.class)
    @Column(name = "codigo_cifrado", nullable = false, length = 128)
    private String codigo;

    @Column(name = "codigo_hash", nullable = false, unique = true, length = 64)
    private String codigoHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoClave estado = EstadoClave.DISPONIBLE;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

    @PrePersist
    void alCrear() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }

}
