package config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import model.seguridad.Rol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// Datos minimos para el servidor: fuera del perfil "dev" solo se crea el administrador,
// y solo si la BD no tiene usuarios. Los datos de demo siguen en DatosIniciales (perfil dev).
@Component
@Profile("!dev")
@RequiredArgsConstructor
public class DatosAdmin implements CommandLineRunner {

    private final EntityManager em;
    private final PasswordEncoder codificador;

    // Se configuran en el application.properties del servidor, nunca en el repo
    @Value("${app.admin.email:admin@luduserp.cat}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Override
    @Transactional
    public void run(String... args) {
        // Si ya hay usuarios no se crea nada
        Long cuantos = em.createQuery("select count(u) from Usuario u", Long.class).getSingleResult();
        if (cuantos > 0) {
            return;
        }

        // Sin contrasena configurada no se arranca: asi nunca queda un admin con una contrasena conocida
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Falta app.admin.password: es la contrasena del administrador inicial");
        }

        // Departamentos
        Departamento direccion = departamento("Direccion", "Gerencia y administracion del sistema");
        departamento("Producto", "Catalogo, fichas, precios y publicaciones");
        departamento("Atencion al cliente", "Soporte, pedidos y reembolsos");
        departamento("Almacen", "Claves de licencia y aprovisionamiento");
        departamento("Compras", "Ordenes de compra y relacion con proveedores");
        departamento("Calidad", "Revision de solicitudes de publicacion");
        departamento("Marketing", "Campanas, descuentos y novedades");
        departamento("Finanzas", "Facturacion, cobros y cierres");
        departamento("Sistemas", "Infraestructura, copias y seguridad");
        departamento("Legal", "Contratos, licencias y proteccion de datos");

        Empleado admin = new Empleado();
        admin.setEmail(email);
        admin.setPasswordHash(codificador.encode(password));
        admin.setRol(Rol.ADMIN);
        admin.setNombre("Administrador");
        admin.setApellidos("del sistema");
        admin.setDepartamento(direccion);
        admin.setCargo("Administrador del sistema");
        // NIF e IBAN se cifran al guardar; el hash permite buscar por NIF
        admin.setNif("12345678Z");
        admin.setNifHash(IndiceCiego.hash("12345678Z"));
        admin.setIban("ES9121000418450200051332");
        admin.setFechaContratacion(LocalDate.now());
        em.persist(admin);
    }

    private Departamento departamento(String nombre, String descripcion) {
        Departamento d = new Departamento();
        d.setNombre(nombre);
        d.setDescripcion(descripcion);
        em.persist(d);
        return d;
    }
}