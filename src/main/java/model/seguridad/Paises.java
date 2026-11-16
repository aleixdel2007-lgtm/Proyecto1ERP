package model.seguridad;

/**
 * Paises que se pueden elegir en el perfil. En la base de datos se guarda el nombre en
 * castellano (getNombre), que es lo que ya habia: "España", no "ESPAÑA".
 */
public enum Paises {
    ESPAÑA("España"),
    FRANCIA("Francia"),
    ALEMANIA("Alemania"),
    ITALIA("Italia"),
    PORTUGAL("Portugal"),
    REINO_UNIDO("Reino Unido"),
    ESTADOS_UNIDOS("Estados Unidos"),
    MEXICO("México"),
    ARGENTINA("Argentina"),
    COLOMBIA("Colombia"),
    CHILE("Chile"),
    PERU("Perú"),
    BRASIL("Brasil"),
    VENEZUELA("Venezuela"),
    URUGUAY("Uruguay"),
    ECUADOR("Ecuador"),
    CHINA("China"),
    JAPON("Japón"),
    INDIA("India"),
    AUSTRALIA("Australia"),
    CANADA("Canadá"),
    MARRUECOS("Marruecos"),
    ANDORRA("Andorra"),
    BELGICA("Bélgica"),
    SUIZA("Suiza"),
    PAISES_BAJOS("Países Bajos"),
    SUECIA("Suecia"),
    NORUEGA("Noruega"),
    DINAMARCA("Dinamarca"),
    IRLANDA("Irlanda"),
    POLONIA("Polonia"),
    GRECIA("Grecia"),
    TURQUIA("Turquía"),
    RUSIA("Rusia"),
    COREA_DEL_SUR("Corea del Sur"),
    SUDAFRICA("Sudáfrica");

    private final String nombre;

    Paises(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
