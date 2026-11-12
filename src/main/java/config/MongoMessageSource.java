package config;

import dao.TraduccionDao;
import model.traduccion.Traduccion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Los textos de la interfaz viven en Mongo. Este MessageSource es lo que Thymeleaf consulta
 * con #{clave}, asi que las plantillas no saben de donde salen.
 *
 * Se llama "messageSource" a proposito: con ese nombre Spring deja de crear el suyo, que lee
 * ficheros .properties. Todas las traducciones se cargan una vez en memoria (son pocas) y no
 * se consulta Mongo por cada texto de cada pagina. Si alguien las edita en Mongo hay que
 * llamar a recargar().
 */
@Component("messageSource")
public class MongoMessageSource extends AbstractMessageSource {

    private static final Logger log = LoggerFactory.getLogger(MongoMessageSource.class);
    private static final String IDIOMA_BASE = "es";

    private final TraduccionDao dao;

    // idioma -> (clave -> texto). Se sustituye entera al recargar.
    private volatile Map<String, Map<String, String>> cache;

    public MongoMessageSource(TraduccionDao dao) {
        this.dao = dao;
    }

    public synchronized void recargar() {
        Map<String, Map<String, String>> nueva = new HashMap<>();
        try {
            for (Traduccion t : dao.findAll()) {
                nueva.computeIfAbsent(t.getIdioma(), i -> new HashMap<>()).put(t.getClave(), t.getTexto());
            }
        } catch (RuntimeException e) {
            // Si Mongo no responde la web sigue viva, con ??clave?? en lugar de textos
            log.warn("No se han podido cargar las traducciones de Mongo", e);
        }
        this.cache = nueva;
    }

    private String buscar(String clave, Locale locale) {
        Map<String, Map<String, String>> actual = cache;
        if (actual == null) {
            recargar();
            actual = cache;
        }
        Map<String, String> delIdioma = actual.get(locale.getLanguage());
        String texto = delIdioma == null ? null : delIdioma.get(clave);
        if (texto == null) {
            // Si falta en ese idioma se ensena el castellano antes que nada
            Map<String, String> base = actual.get(IDIOMA_BASE);
            texto = base == null ? null : base.get(clave);
        }
        return texto;
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return buscar(code, locale);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String texto = buscar(code, locale);
        return texto == null ? null : createMessageFormat(texto, locale);
    }
}
