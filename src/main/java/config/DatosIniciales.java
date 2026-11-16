package config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Categoria;
import model.catalogo.Plataforma;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import model.seguridad.Cliente;
import model.seguridad.Colaborador;
import model.seguridad.Departamento;
import model.seguridad.Rol;
import model.seguridad.Empleado;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private static final String PASSWORD_INICIAL = "luduserp";

    private final EntityManager em;
    private final PasswordEncoder codificador;

    @Override
    @Transactional
    public void run(String... args) {
        Long cuantos = em.createQuery("select count(u) from Usuario u", Long.class).getSingleResult();
        if (cuantos > 0) {
            return;
        }

        Departamento direccion = departamento("Direccion", "Gerencia y administracion del sistema");
        Departamento producto = departamento("Producto", "Catalogo, fichas, precios y publicaciones");
        Departamento atencion = departamento("Atencion al cliente", "Soporte, pedidos y reembolsos");
        Departamento almacen = departamento("Almacen", "Claves de licencia y aprovisionamiento");
        departamento("Compras", "Ordenes de compra y relacion con proveedores");
        departamento("Calidad", "Revision de solicitudes de publicacion");
        departamento("Marketing", "Campanas, descuentos y novedades");
        departamento("Finanzas", "Facturacion, cobros y cierres");
        departamento("Sistemas", "Infraestructura, copias y seguridad");
        departamento("Legal", "Contratos, licencias y proteccion de datos");

        empleado("admin@luduserp.cat", "Aleix", "Delgado", Rol.ADMIN,
                direccion, "Administrador del sistema", "12345678Z",
                "ES9121000418450200051332", LocalDate.of(2024, 9, 2));

        empleado("gestor.productos@luduserp.cat", "Gestor", "de Productos", Rol.GESTOR_PRODUCTOS,
                producto, "Gestor de productos", "11111111H",
                "ES7100302053091234567895", LocalDate.of(2025, 1, 13));

        empleado("gestor.clientes@luduserp.cat", "Gestor", "de Clientes", Rol.GESTOR_CLIENTES,
                atencion, "Gestor de clientes", "24691348C",
                "ES2200492352082414205416", LocalDate.of(2025, 3, 3));

        empleado("gestor.inventario@luduserp.cat", "Gestor", "de Inventario", Rol.GESTOR_INVENTARIO,
                almacen, "Gestor de inventario", "37037018A",
                "ES6000491500051234567892", LocalDate.of(2025, 6, 16));

        Cliente ana = cliente("ana.torres@example.com", "Ana", "Torres", "600111222",
                "Carrer de Mallorca 134", "Barcelona", LocalDate.of(1996, 4, 11));
        Cliente bruno = cliente("bruno.gil@example.com", "Bruno", "Gil", "600333444",
                "Avinguda Diagonal 477", "Barcelona", LocalDate.of(1989, 11, 30));
        Cliente carla = cliente("carla.ruiz@example.com", "Carla", "Ruiz", "600555666",
                "Calle Gran Via 28", "Madrid", LocalDate.of(2001, 2, 7));
        cliente("diego.mas@example.com", "Diego", "Mas", "600777888",
                "Carrer de Colom 9", "Valencia", LocalDate.of(1993, 8, 22));

        Proveedor rheinspiel = proveedor("Rheinspiel", "Rheinspiel GmbH", "A36666642",
                "ES1000492352082414205402", "vertrieb@rheinspiel.de", "Alemania");
        Proveedor albion = proveedor("Albion Games", "Albion Games Ltd", "F45555521",
                "ES1000492352082414205403", "sales@albiongames.co.uk", "Reino Unido");
        Proveedor iberia = proveedor("Distribuciones Iberia", "Distribuciones Iberia SA", "Q7222216I",
                "ES1000492352082414205406", "pedidos@disiberia.es", "España");
        Proveedor kanal = proveedor("Kanal Publishing", "Kanal Publishing BV", "P8999992D",
                "ES1000492352082414205408", "info@kanalpublishing.nl", "Países Bajos");

        Colaborador jordi = colaborador("jordi.bastida@example.com", "Jordi", "Bastida", "España");
        Colaborador aina = colaborador("aina.querol@example.com", "Aina", "Querol", "España");
        Colaborador victor = colaborador("victor.salas@example.com", "Victor", "Salas", "España");
        colaborador("lucia.herrero@example.com", "Lucia", "Herrero", "España");
        colaborador("marc.ibanez@example.com", "Marc", "Ibanez", "España");
        colaborador("elsa.riquelme@example.com", "Elsa", "Riquelme", "España");

        Videojuego eclipse = videojuego("Eclipse Protocol",
                "RPG tactico por turnos en una estacion orbital a la deriva: cada decision gasta oxigeno.",
                iberia, null, "59.99", "44.99", 16, LocalDate.of(2026, 3, 14),
                List.of("RPG", "ESTRATEGIA"), List.of("PC"));
        videojuego("Neon Drifter",
                "Carreras nocturnas por una ciudad infinita, sin frenos y con la radio puesta.",
                rheinspiel, null, "24.99", null, 7, LocalDate.of(2025, 11, 2),
                List.of("CARRERAS"), List.of("PC", "NSW"));
        Videojuego ashen = videojuego("Ashen Crown",
                "Accion medieval en un reino que se cae a pedazos y una corona que nadie quiere.",
                albion, null, "69.99", null, 18, LocalDate.of(2026, 6, 20),
                List.of("ACCION", "AVENTURA"), List.of("PS5", "XSX"));
        Videojuego mycelium = videojuego("Mycelium",
                "Aventura contemplativa bajo tierra: el bosque crece mientras tu duermes.",
                rheinspiel, null, "19.99", "14.99", 12, LocalDate.of(2026, 1, 30),
                List.of("INDIE", "AVENTURA"), List.of("NSW"));
        Videojuego orbital = videojuego("Orbital Foundry",
                "Simulador de fabrica en orbita baja. Si la cadena se para, el turno se acaba.",
                kanal, null, "39.99", "34.99", 3, LocalDate.of(2025, 9, 12),
                List.of("SIMULACION"), List.of("PC"));
        Videojuego lastSignal = videojuego("Last Signal",
                "Terror en una estacion de radio aislada. La senal responde, y no deberia.",
                iberia, null, "29.99", null, 18, LocalDate.of(2026, 4, 4),
                List.of("TERROR"), List.of("PC", "XSX"));
        videojuego("Ironline Tactics",
                "Estrategia por turnos con trenes blindados y muy pocos raíles.",
                albion, null, "39.99", null, 12, LocalDate.of(2026, 2, 18),
                List.of("ESTRATEGIA"), List.of("PC"));

        videojuego("Dust Harbour",
                "Un puerto abandonado, una linterna y catorce minutos de bateria.",
                null, jordi, "0.00", null, 12, LocalDate.of(2026, 10, 15),
                List.of("AVENTURA"), List.of("PC"));
        videojuego("Hollow Transit",
                "Metro subterraneo, vagones vacios y algo que cambia de anden cuando no miras.",
                null, aina, "0.00", null, 7, LocalDate.of(2026, 10, 27),
                List.of("INDIE", "TERROR"), List.of("PC"));
        Videojuego nightjar = videojuego("Nightjar",
                "Puzles cortos de logica nocturna, uno por cada noche del mes.",
                null, victor, "2.99", "1.99", 3, LocalDate.of(2026, 9, 30),
                List.of("PUZZLE"), List.of("PC"));

        clave(eclipse, ana, "ECLP-7H2M-44QX-ZB18", LocalDateTime.of(2026, 10, 22, 19, 12));
        clave(mycelium, ana, "MYCL-3T9K-21RF-WD07", LocalDateTime.of(2026, 10, 22, 19, 12));
        clave(nightjar, ana, "NGTJ-6B4V-90LM-QA55", LocalDateTime.of(2026, 10, 28, 9, 41));
        clave(ashen, bruno, "ASHN-2P8D-63XC-KV92", LocalDateTime.of(2026, 10, 19, 17, 3));
        clave(orbital, bruno, "ORBF-9W5S-12NJ-TG40", LocalDateTime.of(2026, 10, 19, 17, 3));
        clave(lastSignal, carla, "LSTS-5M7Q-38YH-RP61", LocalDateTime.of(2026, 11, 2, 12, 27));

        claveLibre(eclipse, "ECLP-1A1A-2B2B-3C3C");
        claveLibre(eclipse, "ECLP-4D4D-5E5E-6F6F");
        claveLibre(lastSignal, "LSTS-7G7G-8H8H-9I9I");
        
                // Orbital: 4 libres. Comprar una deja 3 y SI avisa (umbral: 3 o menos)
        claveLibre(orbital, "ORBF-1A1A-2B2B-3C3C");
        claveLibre(orbital, "ORBF-4D4D-5E5E-6F6F");
        claveLibre(orbital, "ORBF-7G7G-8H8H-9I9I");
        claveLibre(orbital, "ORBF-0J0J-1K1K-2L2L");
        
                // Stock de demo. Cada juego se va a comprar con un cliente que aun no lo tiene
        // Ashen: 6 libres -> comprar una deja 5, NO avisa (para enseñar que no salta siempre)
        claveLibre(ashen, "ASHN-1A1A-2B2B-3C3C");
        claveLibre(ashen, "ASHN-4D4D-5E5E-6F6F");
        claveLibre(ashen, "ASHN-7G7G-8H8H-9I9I");
        claveLibre(ashen, "ASHN-0J0J-1K1K-2L2L");
        claveLibre(ashen, "ASHN-3M3M-4N4N-5O5O");
        claveLibre(ashen, "ASHN-6P6P-7Q7Q-8R8R");
        // Nightjar: 4 libres -> comprar una deja 3, SI avisa
        claveLibre(nightjar, "NGTJ-1A1A-2B2B-3C3C");
        claveLibre(nightjar, "NGTJ-4D4D-5E5E-6F6F");
        claveLibre(nightjar, "NGTJ-7G7G-8H8H-9I9I");
        claveLibre(nightjar, "NGTJ-0J0J-1K1K-2L2L");
        // Mycelium: 2 libres -> comprar una deja 1, SI avisa
        claveLibre(mycelium, "MYCL-5M5M-6N6N-7O7O");
        claveLibre(mycelium, "MYCL-8P8P-9Q9Q-0R0R");
        
        claveAnulada(mycelium, "MYCL-0J0J-1K1K-2L2L");
    }

    private Departamento departamento(String nombre, String descripcion) {
        Departamento d = new Departamento();
        d.setNombre(nombre);
        d.setDescripcion(descripcion);
        em.persist(d);
        return d;
    }

    private Empleado empleado(String email, String nombre, String apellidos, Rol rol,
                              Departamento departamento, String cargo, String nif,
                              String iban, LocalDate fechaContratacion) {
        Empleado t = new Empleado();
        t.setEmail(email);
        t.setPasswordHash(codificador.encode(PASSWORD_INICIAL));
        t.setRol(rol);
        t.setNombre(nombre);
        t.setApellidos(apellidos);
        t.setDepartamento(departamento);
        t.setCargo(cargo);
        t.setNif(nif);
        t.setNifHash(IndiceCiego.hash(nif));
        t.setIban(iban);
        t.setFechaContratacion(fechaContratacion);
        em.persist(t);
        return t;
    }

    private Cliente cliente(String email, String nombre, String apellidos, String telefono,
                            String direccion, String ciudad, LocalDate fechaNacimiento) {
        Cliente c = new Cliente();
        c.setEmail(email);
        c.setPasswordHash(codificador.encode(PASSWORD_INICIAL));
        c.setNombre(nombre);
        c.setApellidos(apellidos);
        c.setTelefono(telefono);
        c.setDireccion(direccion);
        c.setCiudad(ciudad);
        c.setPais("España");
        c.setFechaNacimiento(fechaNacimiento);
        em.persist(c);
        return c;
    }

    private Colaborador colaborador(String email, String nombre, String apellidos, String pais) {
        Colaborador c = new Colaborador();
        c.setEmail(email);
        c.setPasswordHash(codificador.encode(PASSWORD_INICIAL));
        c.setNombre(nombre);
        c.setApellidos(apellidos);
        c.setPais(pais);
        c.setDebeCambiarPassword(true);
        em.persist(c);
        return c;
    }

    private Proveedor proveedor(String nombreComercial, String razonSocial,
                                String cif, String iban, String email, String pais) {
        Proveedor p = new Proveedor();
        p.setNombreComercial(nombreComercial);
        p.setRazonSocial(razonSocial);
        p.setCif(cif);
        p.setCifHash(IndiceCiego.hash(cif));
        p.setIban(iban);
        p.setEmail(email);
        p.setPais(pais);
        em.persist(p);
        return p;
    }

    private Videojuego videojuego(String titulo, String sinopsis, Proveedor proveedor,
                                  Colaborador colaborador, String precio, String precioDescuento,
                                  int pegi, LocalDate fechaLanzamiento,
                                  List<String> categorias, List<String> plataformas) {
        Videojuego v = new Videojuego();
        v.setTitulo(titulo);
        v.setSinopsis(sinopsis);
        v.setProveedor(proveedor);
        v.setColaborador(colaborador);
        v.setPrecio(new BigDecimal(precio));
        v.setPrecioDescuento(precioDescuento == null ? null : new BigDecimal(precioDescuento));
        v.setPegi(pegi);
        v.setFechaLanzamiento(fechaLanzamiento);
        v.setFechaPublicacion(LocalDateTime.now());
        v.setStockIlimitado(colaborador != null);

        for (String codigo : categorias) {
            v.getCategorias().add(categoria(codigo));
        }

        for (String codigo : plataformas) {
            v.getPlataformas().add(plataforma(codigo));
        }
        em.persist(v);
        return v;
    }

    private ClaveLicencia clave(Videojuego videojuego, Cliente cliente,
                                String codigo, LocalDateTime fechaVenta) {
        ClaveLicencia c = claveLicencia(videojuego, codigo);
        c.setCliente(cliente);
        c.setEstado(EstadoClave.VENDIDA);
        c.setFechaVenta(fechaVenta);
        em.persist(c);
        return c;
    }

    private ClaveLicencia claveLibre(Videojuego videojuego, String codigo) {
        ClaveLicencia c = claveLicencia(videojuego, codigo);
        c.setEstado(EstadoClave.DISPONIBLE);
        em.persist(c);
        return c;
    }

    private ClaveLicencia claveAnulada(Videojuego videojuego, String codigo) {
        ClaveLicencia c = claveLicencia(videojuego, codigo);
        c.setEstado(EstadoClave.ANULADA);
        c.setFechaAnulacion(LocalDateTime.of(2026, 10, 30, 10, 0));
        c.setMotivoAnulacion("Codigo rechazado por el distribuidor");
        em.persist(c);
        return c;
    }

    private ClaveLicencia claveLicencia(Videojuego videojuego, String codigo) {
        ClaveLicencia c = new ClaveLicencia();
        c.setVideojuego(videojuego);
        c.setCodigo(codigo);
        c.setCodigoHash(IndiceCiego.hash(codigo));
        return c;
    }

    private Categoria categoria(String codigo) {
        return em.createQuery("select c from Categoria c where c.codigo = :codigo", Categoria.class)
                .setParameter("codigo", codigo)
                .getSingleResult();
    }

    private Plataforma plataforma(String codigo) {
        return em.createQuery("select p from Plataforma p where p.codigo = :codigo", Plataforma.class)
                .setParameter("codigo", codigo)
                .getSingleResult();
    }
}
