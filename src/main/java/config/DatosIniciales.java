package config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Proveedor;
import model.aprovisionamiento.TipoProveedor;
import model.seguridad.Cliente;
import model.seguridad.CuentaProveedor;
import model.seguridad.Departamento;
import model.seguridad.Rol;
import model.seguridad.Trabajador;
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

        Trabajador admin = trabajador("admin@luduserp.cat", "Aleix", "Delgado", Rol.ADMIN,
                direccion, "Administrador del sistema", "12345678Z",
                "ES9121000418450200051332", LocalDate.of(2024, 9, 2));
        Trabajador gestorProductos = trabajador("producto@luduserp.cat", "Marta", "Ferrer", Rol.GESTOR_PRODUCTOS,
                producto, "Gestora de productos", "23456789A",
                "ES7100302053091234567895", LocalDate.of(2025, 1, 13));
        Trabajador gestorClientes = trabajador("clientes@luduserp.cat", "Oriol", "Serra", Rol.GESTOR_CLIENTES,
                atencion, "Gestor de clientes", "34567890B",
                "ES2200492352082414205416", LocalDate.of(2025, 3, 3));
        Trabajador gestorInventario = trabajador("inventario@luduserp.cat", "Nuria", "Pons", Rol.GESTOR_INVENTARIO,
                almacen, "Gestora de inventario", "45678901C",
                "ES6000491500051234567892", LocalDate.of(2025, 6, 16));

        direccion.setResponsable(admin);
        producto.setResponsable(gestorProductos);
        atencion.setResponsable(gestorClientes);
        almacen.setResponsable(gestorInventario);

        cliente("ana.torres@example.com", "Ana", "Torres", "600111222",
                "Carrer de Mallorca 134", "Barcelona", LocalDate.of(1996, 4, 11));
        cliente("bruno.gil@example.com", "Bruno", "Gil", "600333444",
                "Avinguda Diagonal 477", "Barcelona", LocalDate.of(1989, 11, 30));
        cliente("carla.ruiz@example.com", "Carla", "Ruiz", "600555666",
                "Calle Gran Via 28", "Madrid", LocalDate.of(2001, 2, 7));
        cliente("diego.mas@example.com", "Diego", "Mas", "600777888",
                "Carrer de Colom 9", "Valencia", LocalDate.of(1993, 8, 22));

        CuentaProveedor cuentaTaller = cuentaProveedor("taller3dam@example.com", "Taller 3r DAM A");
        CuentaProveedor cuentaPixel = cuentaProveedor("contacto@pixelforge.dev", "Pixel Forge");

        estudio("Taller 3r DAM A", "Taller 3r DAM A SCCL", "B12345678",
                "ES9420805801101234567891", "taller3dam@example.com", cuentaTaller);
        estudio("Pixel Forge", "Pixel Forge Studio SL", "B87654321",
                "ES1000492352082414205400", "contacto@pixelforge.dev", cuentaPixel);
    }

    private Departamento departamento(String nombre, String descripcion) {
        Departamento d = new Departamento();
        d.setNombre(nombre);
        d.setDescripcion(descripcion);
        em.persist(d);
        return d;
    }

    private Trabajador trabajador(String email, String nombre, String apellidos, Rol rol,
                                  Departamento departamento, String cargo, String nif,
                                  String iban, LocalDate fechaContratacion) {
        Trabajador t = new Trabajador();
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

    private Proveedor estudio(String nombreComercial, String razonSocial, String cif,
                              String iban, String email, CuentaProveedor cuenta) {
        Proveedor p = new Proveedor();
        p.setNombreComercial(nombreComercial);
        p.setRazonSocial(razonSocial);
        p.setTipo(TipoProveedor.ESTUDIO);
        p.setCuenta(cuenta);
        p.setCif(cif);
        p.setCifHash(IndiceCiego.hash(cif));
        p.setIban(iban);
        p.setEmail(email);
        p.setPais("Espana");
        em.persist(p);
        return p;
    }
}
