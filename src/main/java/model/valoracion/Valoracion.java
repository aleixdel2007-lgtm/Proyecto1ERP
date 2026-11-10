package model.valoracion;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import model.catalogo.Videojuego;
import model.seguridad.Cliente;

@Entity
@Table(name = "valoracion")
@Getter
@Setter
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoValoracion estado = EstadoValoracion.ACTIVA;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Min(1)
    @Max(5)
    @Column(name = "valoracion", nullable = false)
    private int valoracion;

    @Transient
    public EstadoValoracion getEstadoEfectivo() {
        if (cliente != null && !cliente.isActivo()) {
            return EstadoValoracion.INACTIVA;
        }
        return estado;
    }
}
