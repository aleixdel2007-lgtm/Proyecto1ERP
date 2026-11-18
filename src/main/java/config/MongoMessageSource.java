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

// Textos de la interfaz guardados en Mongo; Thymeleaf los consulta con #{clave}.
// Se llama "messageSource" para que Spring no cree el suyo, que lee .properties
@Component("messageSource")
public class MongoMessageSource extends AbstractMessageSource {

    private static final Logger log = LoggerFactory.getLogger(MongoMessageSource.class);
    private static final String IDIOMA_BASE = "es";

    private final TraduccionDao dao;

    // idioma -> (clave -> texto); se carga una vez y se sustituye entera al recargar
    private volatile Map<String, Map<String, String>> cache;

    public MongoMessageSource(TraduccionDao dao) {
        this.dao = dao;
    }

    // Vuelve a leer todas las traducciones de Mongo; hay que llamarlo si alguien las edita
    public synchronized void recargar() {
        Map<String, Map<String, String>> nueva = new HashMap<>();
        try {
            for (Traduccion t : dao.findAll()) {
                // Se ignoran los documentos incompletos
                if (t.getClave() == null || t.getTextos() == null) {
                    continue;
                }
                // Reparte los textos del documento en el mapa de cada idioma
                t.getTextos().forEach((idioma, texto) ->
                        nueva.computeIfAbsent(idioma, i -> new HashMap<>()).put(t.getClave(), texto));
            }
        } catch (RuntimeException e) {
            // Si Mongo no responde la web sigue viva, mostrando ??clave?? en vez de textos
            log.warn("No se han podido cargar las traducciones de Mongo", e);
        }
        this.cache = nueva;
    }

    // Busca el texto en el idioma pedido y, si falta, en castellano
    private String buscar(String clave, Locale locale) {
        Map<String, Map<String, String>> actual = cache;
        // Primera consulta: todavia no se ha cargado nada
        if (actual == null) {
            recargar();
            actual = cache;
        }
        Map<String, String> delIdioma = actual.get(locale.getLanguage());
        String texto = delIdioma == null ? null : delIdioma.get(clave);
        if (texto == null) {
            Map<String, String> base = actual.get(IDIOMA_BASE);
            texto = base == null ? null : base.get(clave);
        }
        return texto;
    }

    // Texto sin parametros: se devuelve tal cual
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return buscar(code, locale);
    }

    // Texto con parametros ({0}, {1}...): se devuelve como MessageFormat
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String texto = buscar(code, locale);
        return texto == null ? null : createMessageFormat(texto, locale);
    }
}
