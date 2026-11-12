package model.traduccion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Un texto de la interfaz en un idioma. La clave es la misma que se usa en las plantillas
 * (#{navbar.catalogo}) y el idioma es el codigo de dos letras: es, en o ca.
 */
@Document("traducciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Traduccion {

    @Id
    private String id;
    private String clave;
    private String idioma;
    private String texto;

    public Traduccion(String clave, String idioma, String texto) {
        this.clave = clave;
        this.idioma = idioma;
        this.texto = texto;
    }
}
