package test;

import config.SecurityConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;

// Raiz de los tests de capa web. No se usa Proyecto1ErpApplication porque esa clase lleva
// @EnableJpaRepositories y @EnableMongoRepositories, que obligarian a levantar JPA y Mongo
// para probar una pantalla. Aqui solo entran los controladores y la configuracion de
// seguridad de verdad, que es la que decide quien pasa a cada ruta.
//
// El excludeFilters con TypeExcludeFilter es el que @SpringBootApplication pone de serie:
// sin el, @WebMvcTest no puede limitar la prueba al controlador que se le pide y Spring
// intenta construir los veinte controladores del proyecto con todos sus servicios.
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "controller",
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
@Import(SecurityConfig.class)
class AplicacionDeTest {

    // En los tests los textos se leen de los .properties y no de Mongo, que es lo que hace
    // MongoMessageSource al ejecutar la aplicacion. Asi las pruebas no necesitan base de datos
    // y, de paso, fallan si una clave no existe: la plantilla pintaria ??la.clave_es??
    @Bean
    MessageSource messageSource() {

        ResourceBundleMessageSource textos = new ResourceBundleMessageSource();

        textos.setBasename("i18n/textos");
        textos.setDefaultEncoding("UTF-8");
        textos.setFallbackToSystemLocale(false);

        return textos;
    }
}
