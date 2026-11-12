package model.traduccion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Un texto de la interfaz con todas sus traducciones. La clave es la misma que se usa en las
 * plantillas (#{navbar.catalogo}) y textos va de idioma (es, en, ca) a texto:
 * { clave: "navbar.cesta", textos: { es: "Cesta", en: "Basket", ca: "Cistella" } }
 */
@Document("traducciones")
@Getter
@Setter
@NoArgsConstructor
public class Traduccion {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clave;

    private Map<String, String> textos = new HashMap<>();

    public Traduccion(String clave) {
        this.clave = clave;
    }
}
