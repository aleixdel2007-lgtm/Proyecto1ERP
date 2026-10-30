package model.publicacion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Categoria;
import model.catalogo.Plataforma;
import model.catalogo.Videojuego;
import model.seguridad.Empleado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "solicitud_publicacion")
@Getter
@Setter
public class SolicitudPublicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE_REVISION;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "sinopsis", columnDefinition = "TEXT")
    private String sinopsis;

    @Column(name = "pegi", nullable = false)
    private int pegi;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "solicitud_categoria",
            joinColumns = @JoinColumn(name = "solicitud_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "solicitud_plataforma",
            joinColumns = @JoinColumn(name = "solicitud_id"),
            inverseJoinColumns = @JoinColumn(name = "plataforma_id"))
    private Set<Plataforma> plataformas = new HashSet<>();

    @Column(name = "precio_propuesto", precision = 10, scale = 2)
    private BigDecimal precioPropuesto;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 255)
    private String rutaArchivo;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "sha256", length = 64)
    private String sha256;

    @Column(name = "notas_estudio", columnDefinition = "TEXT")
    private String notasEstudio;

    @Column(name = "numero_envio", nullable = false)
    private int numeroEnvio = 1;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videojuego_id", unique = true)
    private Videojuego videojuego;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden, id")
    private List<ImagenSolicitud> imagenes = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }
    }
}
