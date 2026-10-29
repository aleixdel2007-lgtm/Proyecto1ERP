package dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import model.aprovisionamiento.TipoProveedor;

@Getter
@Setter
public class ProveedorFormulario {

    private Long id;

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 120, message = "Como maximo 120 caracteres")
    private String nombreComercial;

    @NotBlank(message = "La razon social es obligatoria")
    @Size(max = 150, message = "Como maximo 150 caracteres")
    private String razonSocial;

    @NotNull(message = "Hay que indicar el tipo de proveedor")
    private TipoProveedor tipo;

    @NotBlank(message = "El CIF es obligatorio")
    @Size(max = 20, message = "Como maximo 20 caracteres")
    private String cif;

    @Size(max = 34, message = "Un IBAN tiene como maximo 34 caracteres")
    private String iban;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 180, message = "Como maximo 180 caracteres")
    private String email;

    @Size(max = 20, message = "Como maximo 20 caracteres")
    private String telefono;

    @Size(max = 60, message = "Como maximo 60 caracteres")
    private String pais;

    private boolean activo = true;
}
