package model.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "videojuego")
@Getter
@Setter
public class Videojuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @Column(name = "sinopsis", columnDefinition = "TEXT")
    private String sinopsis;

    @Column(name = "estudio", length = 120)
    private String estudio;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "precio_anterior", precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(name = "fecha_lanzamiento")
    private LocalDate fechaLanzamiento;

    @Column(name = "pegi", nullable = false)
    private int pegi;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "stock_ilimitado", nullable = false)
    private boolean stockIlimitado = false;

    @Column(name = "umbral_stock_bajo", nullable = false)
    private int umbralStockBajo = 10;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "videojuego_categoria",
            joinColumns = @JoinColumn(name = "videojuego_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "videojuego_plataforma",
            joinColumns = @JoinColumn(name = "videojuego_id"),
            inverseJoinColumns = @JoinColumn(name = "plataforma_id"))
    private Set<Plataforma> plataformas = new HashSet<>();

    @OneToMany(mappedBy = "videojuego")
    @OrderBy("orden, id")
    private List<ImagenVideojuego> imagenes = new ArrayList<>();

}
