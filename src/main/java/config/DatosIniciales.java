package config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Proveedor;
import model.aprovisionamiento.TipoProveedor;
import model.seguridad.Cliente;
import model.seguridad.CuentaProveedor;
import model.seguridad.Departamento;
import model.seguridad.Rol;
import model.seguridad.Empleado;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
        Departamento compras = departamento("Compras", "Ordenes de compra y relacion con proveedores");
        Departamento calidad = departamento("Calidad", "Revision de solicitudes de publicacion");
        Departamento marketing = departamento("Marketing", "Campanas, descuentos y novedades");
        Departamento finanzas = departamento("Finanzas", "Facturacion, cobros y cierres");
        Departamento sistemas = departamento("Sistemas", "Infraestructura, copias y seguridad");
        Departamento legal = departamento("Legal", "Contratos, licencias y proteccion de datos");

        Empleado admin = empleado("admin@luduserp.cat", "Aleix", "Delgado", Rol.ADMIN,
                direccion, "Administrador del sistema", "12345678Z",
                "ES9121000418450200051332", LocalDate.of(2024, 9, 2));

        Empleado producto1 = empleado("marta.ferrer@luduserp.cat", "Marta", "Ferrer", Rol.GESTOR_PRODUCTOS,
                producto, "Gestora de productos", "11111111H",
                "ES7100302053091234567895", LocalDate.of(2025, 1, 13));
        Empleado producto2 = empleado("pau.vidal@luduserp.cat", "Pau", "Vidal", Rol.GESTOR_PRODUCTOS,
                producto, "Gestor de catalogo", "13580245X",
                "ES7100302053091234567896", LocalDate.of(2025, 1, 20));
        empleado("laia.soler@luduserp.cat", "Laia", "Soler", Rol.GESTOR_PRODUCTOS,
                producto, "Editora de fichas", "14814812Y",
                "ES7100302053091234567897", LocalDate.of(2025, 2, 3));
        empleado("marc.rius@luduserp.cat", "Marc", "Rius", Rol.GESTOR_PRODUCTOS,
                calidad, "Revisor de publicaciones", "16049379W",
                "ES7100302053091234567898", LocalDate.of(2025, 2, 17));
        empleado("gemma.pla@luduserp.cat", "Gemma", "Pla", Rol.GESTOR_PRODUCTOS,
                calidad, "Revisora de contenidos", "17283946K",
                "ES7100302053091234567899", LocalDate.of(2025, 3, 10));
        empleado("ivan.costa@luduserp.cat", "Ivan", "Costa", Rol.GESTOR_PRODUCTOS,
                marketing, "Gestor de novedades", "18518513V",
                "ES7100302053091234567900", LocalDate.of(2025, 4, 7));
        empleado("sara.munoz@luduserp.cat", "Sara", "Munoz", Rol.GESTOR_PRODUCTOS,
                marketing, "Gestora de precios", "19753080J",
                "ES7100302053091234567901", LocalDate.of(2025, 5, 5));
        empleado("joel.bosch@luduserp.cat", "Joel", "Bosch", Rol.GESTOR_PRODUCTOS,
                producto, "Gestor de plataformas", "20987647D",
                "ES7100302053091234567902", LocalDate.of(2025, 6, 2));
        empleado("nil.camps@luduserp.cat", "Nil", "Camps", Rol.GESTOR_PRODUCTOS,
                producto, "Gestor de clasificacion", "22222214M",
                "ES7100302053091234567903", LocalDate.of(2025, 7, 14));
        empleado("berta.lluch@luduserp.cat", "Berta", "Lluch", Rol.GESTOR_PRODUCTOS,
                calidad, "Revisora de imagenes", "23456781R",
                "ES7100302053091234567904", LocalDate.of(2025, 9, 1));

        Empleado clientes1 = empleado("oriol.serra@luduserp.cat", "Oriol", "Serra", Rol.GESTOR_CLIENTES,
                atencion, "Gestor de clientes", "24691348C",
                "ES2200492352082414205416", LocalDate.of(2025, 3, 3));
        Empleado clientes2 = empleado("alba.prats@luduserp.cat", "Alba", "Prats", Rol.GESTOR_CLIENTES,
                atencion, "Agente de soporte", "25925915Q",
                "ES2200492352082414205417", LocalDate.of(2025, 3, 17));
        empleado("hugo.navarro@luduserp.cat", "Hugo", "Navarro", Rol.GESTOR_CLIENTES,
                atencion, "Agente de soporte", "27160482N",
                "ES2200492352082414205418", LocalDate.of(2025, 4, 1));
        empleado("clara.pons@luduserp.cat", "Clara", "Pons", Rol.GESTOR_CLIENTES,
                atencion, "Gestora de reembolsos", "28395049P",
                "ES2200492352082414205419", LocalDate.of(2025, 4, 21));
        empleado("adria.font@luduserp.cat", "Adria", "Font", Rol.GESTOR_CLIENTES,
                atencion, "Gestor de pedidos", "29629616G",
                "ES2200492352082414205420", LocalDate.of(2025, 5, 12));
        empleado("noa.esteve@luduserp.cat", "Noa", "Esteve", Rol.GESTOR_CLIENTES,
                finanzas, "Gestora de cobros", "30864183T",
                "ES2200492352082414205421", LocalDate.of(2025, 6, 9));
        Empleado clientes7 = empleado("eric.roca@luduserp.cat", "Eric", "Roca", Rol.GESTOR_CLIENTES,
                finanzas, "Gestor de facturacion", "32098750L",
                "ES2200492352082414205422", LocalDate.of(2025, 6, 30));
        empleado("julia.mora@luduserp.cat", "Julia", "Mora", Rol.GESTOR_CLIENTES,
                marketing, "Gestora de fidelizacion", "33333317S",
                "ES2200492352082414205423", LocalDate.of(2025, 8, 4));
        empleado("bruno.sala@luduserp.cat", "Bruno", "Sala", Rol.GESTOR_CLIENTES,
                atencion, "Agente de soporte", "34567884B",
                "ES2200492352082414205424", LocalDate.of(2025, 9, 15));
        empleado("ona.gispert@luduserp.cat", "Ona", "Gispert", Rol.GESTOR_CLIENTES,
                legal, "Gestora de reclamaciones", "35802451F",
                "ES2200492352082414205425", LocalDate.of(2025, 10, 6));

        Empleado inventario1 = empleado("nuria.pons@luduserp.cat", "Nuria", "Pons", Rol.GESTOR_INVENTARIO,
                almacen, "Gestora de inventario", "37037018A",
                "ES6000491500051234567892", LocalDate.of(2025, 6, 16));
        Empleado inventario2 = empleado("roger.alsina@luduserp.cat", "Roger", "Alsina", Rol.GESTOR_INVENTARIO,
                almacen, "Gestor de claves", "38271585E",
                "ES6000491500051234567893", LocalDate.of(2025, 6, 23));
        empleado("ariadna.boix@luduserp.cat", "Ariadna", "Boix", Rol.GESTOR_INVENTARIO,
                almacen, "Gestora de existencias", "39506152H",
                "ES6000491500051234567894", LocalDate.of(2025, 7, 7));
        empleado("dani.oliva@luduserp.cat", "Dani", "Oliva", Rol.GESTOR_INVENTARIO,
                compras, "Gestor de recepciones", "40740719Z",
                "ES6000491500051234567895", LocalDate.of(2025, 7, 28));
        empleado("elena.riba@luduserp.cat", "Elena", "Riba", Rol.GESTOR_INVENTARIO,
                compras, "Gestora de ordenes", "41975286X",
                "ES6000491500051234567896", LocalDate.of(2025, 8, 11));
        empleado("pol.ventura@luduserp.cat", "Pol", "Ventura", Rol.GESTOR_INVENTARIO,
                compras, "Gestor de proveedores", "43209853Y",
                "ES6000491500051234567897", LocalDate.of(2025, 8, 25));
        empleado("irene.quer@luduserp.cat", "Irene", "Quer", Rol.GESTOR_INVENTARIO,
                almacen, "Gestora de licencias", "44444420W",
                "ES6000491500051234567898", LocalDate.of(2025, 9, 8));
        Empleado inventario8 = empleado("guim.tena@luduserp.cat", "Guim", "Tena", Rol.GESTOR_INVENTARIO,
                sistemas, "Gestor de copias", "45678987K",
                "ES6000491500051234567899", LocalDate.of(2025, 9, 29));
        empleado("lluis.bada@luduserp.cat", "Lluis", "Bada", Rol.GESTOR_INVENTARIO,
                sistemas, "Gestor de infraestructura", "46913554V",
                "ES6000491500051234567900", LocalDate.of(2025, 10, 13));
        Empleado inventario10 = empleado("carlota.vila@luduserp.cat", "Carlota", "Vila", Rol.GESTOR_INVENTARIO,
                legal, "Gestora de contratos", "48148121J",
                "ES6000491500051234567901", LocalDate.of(2025, 10, 27));

        direccion.setResponsable(admin);
        producto.setResponsable(producto1);
        atencion.setResponsable(clientes1);
        almacen.setResponsable(inventario1);
        compras.setResponsable(producto2);
        calidad.setResponsable(clientes2);
        marketing.setResponsable(inventario2);
        finanzas.setResponsable(clientes7);
        sistemas.setResponsable(inventario8);
        legal.setResponsable(inventario10);

        cliente("ana.torres@example.com", "Ana", "Torres", "600111222",
                "Carrer de Mallorca 134", "Barcelona", LocalDate.of(1996, 4, 11));
        cliente("bruno.gil@example.com", "Bruno", "Gil", "600333444",
                "Avinguda Diagonal 477", "Barcelona", LocalDate.of(1989, 11, 30));
        cliente("carla.ruiz@example.com", "Carla", "Ruiz", "600555666",
                "Calle Gran Via 28", "Madrid", LocalDate.of(2001, 2, 7));
        cliente("diego.mas@example.com", "Diego", "Mas", "600777888",
                "Carrer de Colom 9", "Valencia", LocalDate.of(1993, 8, 22));

        proveedor("Taller 3r DAM A", "Taller 3r DAM A SCCL", TipoProveedor.ESTUDIO, "B10000008",
                "ES9420805801101234567891", "taller3dam@example.com", "Espana");
        proveedor("Pixel Forge", "Pixel Forge Studio SL", TipoProveedor.ESTUDIO, "A18888883",
                "ES1000492352082414205400", "contacto@pixelforge.dev", "Espana");
        proveedor("Nordic Bytes", "Nordic Bytes AB", TipoProveedor.ESTUDIO, "B27777762",
                "ES1000492352082414205401", "hola@nordicbytes.se", "Suecia");
        proveedor("Rheinspiel", "Rheinspiel GmbH", TipoProveedor.DISTRIBUIDOR, "A36666642",
                "ES1000492352082414205402", "vertrieb@rheinspiel.de", "Alemania");
        proveedor("Albion Games", "Albion Games Ltd", TipoProveedor.DISTRIBUIDOR, "F45555521",
                "ES1000492352082414205403", "sales@albiongames.co.uk", "Reino Unido");
        proveedor("Lowlands Interactive", "Lowlands Interactive BV", TipoProveedor.ESTUDIO, "B54444401",
                "ES1000492352082414205404", "info@lowlands.nl", "Paises Bajos");
        proveedor("Estudio Mediterraneo", "Estudio Mediterraneo SL", TipoProveedor.ESTUDIO, "A63333280",
                "ES1000492352082414205405", "contacto@mediterraneo.es", "Espana");
        proveedor("Distribuciones Iberia", "Distribuciones Iberia SA", TipoProveedor.DISTRIBUIDOR, "Q7222216I",
                "ES1000492352082414205406", "pedidos@disiberia.es", "Espana");
        proveedor("Vikingr Studio", "Vikingr Studio AB", TipoProveedor.ESTUDIO, "B81111049",
                "ES1000492352082414205407", "post@vikingr.se", "Suecia");
        proveedor("Kanal Publishing", "Kanal Publishing BV", TipoProveedor.DISTRIBUIDOR, "P8999992D",
                "ES1000492352082414205408", "info@kanalpublishing.nl", "Paises Bajos");
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
        c.setPais("Espana");
        c.setFechaNacimiento(fechaNacimiento);
        em.persist(c);
        return c;
    }

    private CuentaProveedor cuentaProveedor(String email, String nombre) {
        CuentaProveedor c = new CuentaProveedor();
        c.setEmail(email);
        c.setPasswordHash(codificador.encode(PASSWORD_INICIAL));
        c.setNombre(nombre);
        c.setDebeCambiarPassword(true);
        em.persist(c);
        return c;
    }

    // Crea el proveedor junto con su cuenta de acceso, que comparte el correo
    private Proveedor proveedor(String nombreComercial, String razonSocial, TipoProveedor tipo,
                                String cif, String iban, String email, String pais) {
        Proveedor p = new Proveedor();
        p.setNombreComercial(nombreComercial);
        p.setRazonSocial(razonSocial);
        p.setTipo(tipo);
        p.setCuenta(cuentaProveedor(email, nombreComercial));
        p.setCif(cif);
        p.setCifHash(IndiceCiego.hash(cif));
        p.setIban(iban);
        p.setEmail(email);
        p.setPais(pais);
        em.persist(p);
        return p;
    }
}
