package model.seguridad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cuenta_proveedor")
@Getter
@Setter
public class CuentaProveedor extends Usuario {

    public CuentaProveedor() {
        setRol(Rol.PROVEEDOR);
    }
}
