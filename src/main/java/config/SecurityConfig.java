package config;

import dao.UsuarioDao;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Las contrasenas se guardan con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Carga el usuario de la BD; el "username" del login es el email
    @Bean
    public UserDetailsService userDetailsService(UsuarioDao usuarioDao) {
        return email -> {
            Usuario u = usuarioDao.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + email));
            return User.builder()
                    .username(u.getEmail())
                    .password(u.getPasswordHash())
                    .roles(u.getRol().name())
                    // Una cuenta inactiva no puede entrar (lo detecta el failureHandler)
                    .disabled(!u.isActivo())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Las reglas se evaluan en orden: la primera que coincide gana
                .authorizeHttpRequests(auth -> auth
                // Recursos estaticos y paginas publicas
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/registro", "/solicitar-activacion").permitAll()
                // Panel ERP
                .requestMatchers("/departamentos/**").hasRole("ADMIN")
                .requestMatchers("/empleados/**").hasRole("ADMIN")
                .requestMatchers("/catalogo/videojuegos/**").hasAnyRole("GESTOR_PRODUCTOS", "ADMIN")
                .requestMatchers("/catalogo/categorias/**").hasAnyRole("GESTOR_PRODUCTOS", "ADMIN")
                .requestMatchers("/catalogo/plataformas/**").hasAnyRole("GESTOR_PRODUCTOS", "ADMIN")
                .requestMatchers("/compras/**").hasAnyRole("ADMIN", "GESTOR_INVENTARIO")
                .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "GESTOR_INVENTARIO")
                // Las claves son el stock y las lleva inventario
                .requestMatchers("/inventario/**").hasAnyRole("ADMIN", "GESTOR_INVENTARIO")
                // Estas tres van antes de la regla general de /tienda/**; la descarga se queda en la general
                // porque un juego gratis lo puede bajar cualquiera que entre en la tienda
                .requestMatchers("/tienda/biblioteca/**").hasRole("CLIENTE")
                .requestMatchers("/tienda/cesta/**").hasRole("CLIENTE")
                .requestMatchers("/tienda/pago/**").hasRole("CLIENTE")
                .requestMatchers("/tienda", "/tienda/**").hasAnyRole("CLIENTE", "COLABORADOR", "ADMIN")
                .requestMatchers("/valoraciones/**").hasRole("CLIENTE")
                .requestMatchers("/home").hasAnyRole("ADMIN", "GESTOR_CLIENTES", "GESTOR_PRODUCTOS", "GESTOR_INVENTARIO")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/colaborador/**").hasRole("COLABORADOR")
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/clientes/**").hasAnyRole("GESTOR_CLIENTES", "ADMIN")
                // Cualquier ruta no listada exige al menos estar logueado
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                // Clientes y colaboradores van a la tienda; el resto de roles, a /home
                .successHandler((request, response, authentication) -> {
                    boolean vaATienda = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.CLIENTE.name())
                            || a.getAuthority().equals("ROLE_" + Rol.COLABORADOR.name()));
                    response.sendRedirect(request.getContextPath() + (vaATienda ? "/tienda" : "/home"));
                })
                // Si el login falla se distingue el motivo: cuenta inactiva u otra cosa
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof DisabledException) {
                        // Se pasa el email escrito para usarlo en la solicitud de activacion
                        String email = URLEncoder.encode(request.getParameter("username"), StandardCharsets.UTF_8);
                        response.sendRedirect(request.getContextPath() + "/login?inactivo&email=" + email);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login?error");
                    }
                })
                .permitAll()
                )
                .exceptionHandling(ex -> ex
                // Acceso denegado: un cliente o colaborador que escribe /home a mano va a su tienda, no al 403
                .accessDeniedHandler((request, response, denied) -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    boolean vaATienda = auth != null && auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.CLIENTE.name())
                            || a.getAuthority().equals("ROLE_" + Rol.COLABORADOR.name()));
                    // Ruta sin el contexto de la aplicacion, para compararla
                    String ruta = request.getRequestURI().substring(request.getContextPath().length());
                    if (vaATienda && ruta.equals("/home")) {
                        response.sendRedirect(request.getContextPath() + "/tienda");
                    } else {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                })
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .permitAll()
                );

        return http.build();
    }
}
