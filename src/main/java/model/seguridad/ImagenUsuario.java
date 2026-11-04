package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imagen_usuario")
@Getter
@Setter
public class ImagenUsuario {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "ruta_imagen", nullable = false, length = 255)
    private String rutaImagen;

    @Column(name = "texto_alternativo", length = 160)
    private String textoAlternativo;
}
