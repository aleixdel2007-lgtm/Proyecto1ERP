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

/**
 * Carga en Mongo los textos de src/main/resources/i18n/textos_es|en|ca.properties. Esos
 * ficheros van en git, asi todo el equipo parte de las mismas traducciones. Solo se insertan
 * las que faltan: lo que alguien haya editado en Mongo no se pisa.
 */
@Component
@RequiredArgsConstructor
public class TraduccionesIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TraduccionesIniciales.class);
    private static final List<String> IDIOMAS = List.of("es", "en", "ca");

    private final TraduccionDao dao;
    private final MongoMessageSource messageSource;

    @Override
    public void run(String... args) throws IOException {
        // Un documento por clave. Los que ya existen se completan con los idiomas que les
        // falten, sin tocar los que ya tienen texto.
        Map<String, Traduccion> porClave = new HashMap<>();
        List<Traduccion> formatoViejo = new ArrayList<>();
        for (Traduccion t : dao.findAll()) {
            // Los documentos del formato anterior (uno por clave e idioma, con idioma y texto
            // en vez de textos) se leen sin textos: se borran y se vuelven a crear de los .properties
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
                clavesBase.addAll(claves);
            } else if (!claves.containsAll(clavesBase)) {
                Set<String> faltan = new HashSet<>(clavesBase);
                faltan.removeAll(claves);
                log.warn("Faltan {} traducciones al '{}': {}", faltan.size(), idioma, faltan);
            }
            for (String clave : claves) {
                Traduccion t = porClave.computeIfAbsent(clave, Traduccion::new);
                if (t.getTextos() == null) {
                    t.setTextos(new HashMap<>());
                }
                if (!t.getTextos().containsKey(idioma)) {
                    t.getTextos().put(idioma, textos.getProperty(clave));
                    modificadas.add(clave);
                }
            }
        }

        if (!modificadas.isEmpty()) {
            List<Traduccion> aGuardar = new ArrayList<>();
            modificadas.forEach(c -> aGuardar.add(porClave.get(c)));
            dao.saveAll(aGuardar);
            log.info("Guardadas {} claves de traduccion nuevas o completadas en Mongo", aGuardar.size());
        }
        messageSource.recargar();
    }

    private Properties leer(String idioma) throws IOException {
        Properties p = new Properties();
        // Properties.load(InputStream) leeria ISO-8859-1 y romperia las tildes
        try (Reader r = new InputStreamReader(
                new ClassPathResource("i18n/textos_" + idioma + ".properties").getInputStream(),
                StandardCharsets.UTF_8)) {
            p.load(r);
        }
        return p;
    }
}
