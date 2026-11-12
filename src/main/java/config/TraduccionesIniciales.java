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
import java.util.HashSet;
import java.util.List;
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
        Set<String> existentes = new HashSet<>();
        for (Traduccion t : dao.findAll()) {
            existentes.add(t.getIdioma() + "|" + t.getClave());
        }

        List<Traduccion> nuevas = new ArrayList<>();
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
                if (!existentes.contains(idioma + "|" + clave)) {
                    nuevas.add(new Traduccion(clave, idioma, textos.getProperty(clave)));
                }
            }
        }

        if (!nuevas.isEmpty()) {
            dao.saveAll(nuevas);
            log.info("Insertadas {} traducciones nuevas en Mongo", nuevas.size());
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
