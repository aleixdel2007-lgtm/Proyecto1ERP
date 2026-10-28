package model.ventas;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.catalogo.Videojuego;

@Entity
@Table(name = "linea_cesta",
        uniqueConstraints = @UniqueConstraint(name = "uk_linea_cesta_videojuego",
                columnNames = {"cesta_id", "videojuego_id"}))
@Getter
@Setter
public class LineaCesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cesta_id", nullable = false)
    private Cesta cesta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @Column(nullable = false)
    private int cantidad = 1;
}
