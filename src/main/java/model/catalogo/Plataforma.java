package model.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plataforma")
@Getter
@Setter
public class Plataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;
}
