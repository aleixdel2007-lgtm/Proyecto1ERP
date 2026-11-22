package service.implement;

import dao.UsuarioDao;
import java.time.LocalDate;
import java.util.regex.Pattern;
import model.seguridad.Cliente;
import model.seguridad.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.ClienteService;
import service.interfaces.CuentaService;
import service.interfaces.UsuarioService;

@Service
public class CuentaServiceImpl implements CuentaService {

    private static final int LONGITUD_MINIMA = 8;

    // Registro: minimo 8 caracteres, una mayuscula y un numero
    private static final Pattern PASSWORD_VALIDA = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    // Una mayúscula (también Ñ, À...) y un símbolo especial (ni letra, ni número, ni espacio)
    private static final Pattern TIENE_MAYUSCULA = Pattern.compile("\\p{Lu}");
    private static final Pattern TIENE_SIMBOLO = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder codificador;

    @Override
    @Transactional
    public void registrarCliente(String nombre, String apellidos, String email,
                                 String password, String repetirPassword, String pais,
                                 LocalDate fechaNacimiento, boolean aceptaCondiciones) {

        // El correo se guarda siempre en minusculas y sin espacios
        String correo = email.trim().toLowerCase();

        // Primera comprobacion: que el correo no este ya registrado
        clienteService.comprobarCorreoLibre(correo);

        // Validacion del servidor: no se fia de lo que haya comprobado el navegador
        boolean valido = aceptaCondiciones
                && !nombre.isBlank() && !apellidos.isBlank()
                && password.equals(repetirPassword)
                && PASSWORD_VALIDA.matcher(password).matches();
        if (!valido) {
            throw new IllegalArgumentException("registro.error.datosInvalidos");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre.trim());
        cliente.setApellidos(apellidos.trim());
        cliente.setEmail(correo);
        // La contrasena se guarda cifrada, nunca en claro
        cliente.setPasswordHash(codificador.encode(password));
        cliente.setPais(pais == null || pais.isBlank() ? null : pais);
        cliente.setFechaNacimiento(fechaNacimiento);
        usuarioDao.save(cliente);
    }

    @Override
    @Transactional
    public void cambiarPassword(String email, String passwordActual,
                                String nuevaPassword, String repetirPassword) {

        Usuario usuario = usuarioService.buscarUsuarioXEmail(email);

        // La contraseña actual: matches(claro, hash), nunca encode + equals
        if (!codificador.matches(passwordActual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("password.error.actual");
        }

        if (!nuevaPassword.equals(repetirPassword)) {
            throw new IllegalArgumentException("password.error.noCoincide");
        }

        if (nuevaPassword.length() < LONGITUD_MINIMA) {
            throw new IllegalArgumentException("password.error.corta");
        }

        if (!TIENE_MAYUSCULA.matcher(nuevaPassword).find()) {
            throw new IllegalArgumentException("password.error.mayuscula");
        }

        if (!TIENE_SIMBOLO.matcher(nuevaPassword).find()) {
            throw new IllegalArgumentException("password.error.simbolo");
        }

        if (codificador.matches(nuevaPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("password.error.igual");
        }

        usuario.setPasswordHash(codificador.encode(nuevaPassword));
        usuarioDao.save(usuario);
    }
}
