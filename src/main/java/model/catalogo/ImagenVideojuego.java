package model.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imagen_videojuego")
@Getter
@Setter
public class ImagenVideojuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @Column(nullable = false, length = 255)
    private String ruta;

    @Column(name = "texto_alternativo", length = 160)
    private String textoAlternativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoImagen tipo;

    @Column(nullable = false)
    private int orden;
}
