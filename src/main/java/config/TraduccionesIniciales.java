package config;

import dao.TraduccionDao;
import lombok.RequiredArgsConstructor;
import model.traduccion.Traduccion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// Al arrancar vuelca los .properties de i18n a Mongo, completando lo que falte sin pisar lo existente
@Component
@RequiredArgsConstructor
public class TraduccionesIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TraduccionesIniciales.class);
    private static final List<String> IDIOMAS = List.of("es", "en", "ca");

    private final TraduccionDao dao;
    private final MongoMessageSource messageSource;

    @Override
    public void run(String... args) throws IOException {
        // Un documento por clave, con los textos de todos los idiomas
        Map<String, Traduccion> porClave = new HashMap<>();
        List<Traduccion> formatoViejo = new ArrayList<>();
        for (Traduccion t : dao.findAll()) {
            // El formato anterior (un documento por clave e idioma) se lee sin textos: se borra y se recrea
            if (t.getClave() == null || t.getTextos() == null || t.getTextos().isEmpty()) {
                formatoViejo.add(t);
            } else {
                porClave.put(t.getClave(), t);
            }
        }
        if (!formatoViejo.isEmpty()) {
            dao.deleteAll(formatoViejo);
            log.info("Borrados {} documentos de traduccion del formato anterior", formatoViejo.size());
        }

        Set<String> modificadas = new HashSet<>();
        Set<String> clavesBase = new HashSet<>();
        for (String idioma : IDIOMAS) {
            Properties textos = leer(idioma);
            Set<String> claves = textos.stringPropertyNames();
            if (idioma.equals("es")) {
                // El castellano es la referencia con la que se comparan los otros idiomas
                clavesBase.addAll(claves);
            } else if (!claves.containsAll(clavesBase)) {
                // Se avisa de las claves que existen en castellano y faltan en este idioma
                Set<String> faltan = new HashSet<>(clavesBase);
                faltan.removeAll(claves);
                log.warn("Faltan {} traducciones al '{}': {}", faltan.size(), idioma, faltan);
            }
            for (String clave : claves) {
                Traduccion t = porClave.computeIfAbsent(clave, Traduccion::new);
                if (t.getTextos() == null) {
                    t.setTextos(new HashMap<>());
                }
                // Solo se anade el idioma que falta; lo ya guardado (quiza editado en Mongo) no se toca
                if (!t.getTextos().containsKey(idioma)) {
                    t.getTextos().put(idioma, textos.getProperty(clave));
                    modificadas.add(clave);
                }
            }
        }

        // Solo se guardan los documentos que han cambiado
        if (!modificadas.isEmpty()) {
            List<Traduccion> aGuardar = new ArrayList<>();
            modificadas.forEach(c -> aGuardar.add(porClave.get(c)));
            dao.saveAll(aGuardar);
            log.info("Guardadas {} claves de traduccion nuevas o completadas en Mongo", aGuardar.size());
        }
        // Refresca la cache en memoria con lo que hay ya en Mongo
        messageSource.recargar();
    }

    private Properties leer(String idioma) throws IOException {
        Properties p = new Properties();
        // Se lee como UTF-8: el load(InputStream) por defecto usaria ISO-8859-1 y rompe las tildes
        try (Reader r = new InputStreamReader(
                new ClassPathResource("i18n/textos_" + idioma + ".properties").getInputStream(),
                StandardCharsets.UTF_8)) {
            p.load(r);
        }
        return p;
    }
}
