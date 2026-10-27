package model.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categoria")
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;
}
