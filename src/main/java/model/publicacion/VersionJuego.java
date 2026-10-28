package model.publicacion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.catalogo.Videojuego;

import java.time.LocalDateTime;

@Entity
@Table(name = "version_juego",
        uniqueConstraints = @UniqueConstraint(name = "uk_version_juego_numero",
                columnNames = {"videojuego_id", "numero"}))
@Getter
@Setter
public class VersionJuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 255)
    private String rutaArchivo;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "sha256", length = 64)
    private String sha256;

    @Column(name = "activa", nullable = false)
    private boolean activa = false;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    void alCrear() {
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
    }
}
