package model.ventas;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.seguridad.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cesta")
@Getter
@Setter
public class Cesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @OneToMany(mappedBy = "cesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaCesta> lineas = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        fechaModificacion = fechaCreacion;
    }

    @PreUpdate
    void alModificar() {
        fechaModificacion = LocalDateTime.now();
    }
}
