package model.traduccion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;


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
