package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "colaborador")
@Getter
@Setter
public class Colaborador extends Usuario {

    public Colaborador() {
        setRol(Rol.COLABORADOR);
    }

    @Column(name = "pais", length = 60)
    private String pais;
}
