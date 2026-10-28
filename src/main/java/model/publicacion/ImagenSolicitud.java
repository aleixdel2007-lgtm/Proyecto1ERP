package model.publicacion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.catalogo.TipoImagen;

@Entity
@Table(name = "imagen_solicitud")
@Getter
@Setter
public class ImagenSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudPublicacion solicitud;

    @Column(name = "ruta", nullable = false, length = 255)
    private String ruta;

    @Column(name = "texto_alternativo", length = 160)
    private String textoAlternativo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoImagen tipo;

    @Column(name = "orden", nullable = false)
    private int orden;
}
