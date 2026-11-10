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

    // Definimos un PasswordEncoder usando BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Carga los usuarios desde la base de datos, el "username" del login es el email
    @Bean
    public UserDetailsService userDetailsService(UsuarioDao usuarioDao) {
        return email -> {
            Usuario u = usuarioDao.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + email));
            return User.builder()
                    .username(u.getEmail())
                    .password(u.getPasswordHash())
                    .roles(u.getRol().name())
                    .disabled(!u.isActivo())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/registro", "/solicitar-activacion").permitAll()
                .requestMatchers("/departamentos/**").hasRole("ADMIN")
                .requestMatchers("/empleados/**").hasRole("ADMIN")
                .requestMatchers("/catalogo/videojuegos/**").hasAnyRole("GESTOR_PRODUCTOS", "ADMIN")
                .requestMatchers("/catalogo/categorias/**").hasAnyRole("GESTOR_PRODUCTOS", "ADMIN")
                .requestMatchers("/catalogo/plataformas/**").hasAnyRole("GESTOR_PRODUCTOS", "ADMIN")
                .requestMatchers("/compras/**").hasAnyRole("ADMIN", "GESTOR_INVENTARIO")
                .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "GESTOR_INVENTARIO")
                .requestMatchers("/tienda", "/tienda/**").hasAnyRole("CLIENTE", "COLABORADOR", "ADMIN")
                .requestMatchers("/valoraciones/**").hasRole("CLIENTE")
                .requestMatchers("/home").hasAnyRole("ADMIN", "GESTOR_CLIENTES", "GESTOR_PRODUCTOS", "GESTOR_INVENTARIO")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/colaborador/**").hasRole("COLABORADOR")
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/clientes/**").hasAnyRole("GESTOR_CLIENTES", "ADMIN")
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                // Los clientes y los colaboradores van a la tienda, el resto de roles, a "/home"
                .successHandler((request, response, authentication) -> {
                    boolean vaATienda = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.CLIENTE.name())
                            || a.getAuthority().equals("ROLE_" + Rol.COLABORADOR.name()));
                    response.sendRedirect(request.getContextPath() + (vaATienda ? "/tienda" : "/home"));
                })
                // si el login falla, miramos por que: si es porque la cuenta esta inactiva, avisamos aparte
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof DisabledException) {
                        // guardamos el email que escribio para poder usarlo luego en la solicitud
                        String email = URLEncoder.encode(request.getParameter("username"), StandardCharsets.UTF_8);
                        response.sendRedirect(request.getContextPath() + "/login?inactivo&email=" + email);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login?error");
                    }
                })
                .permitAll()
                )
                .exceptionHandling(ex -> ex
                // Un cliente o un colaborador que escribe /home a mano va a su pagina de inicio, no al 403
                .accessDeniedHandler((request, response, denied) -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    boolean vaATienda = auth != null && auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.CLIENTE.name())
                            || a.getAuthority().equals("ROLE_" + Rol.COLABORADOR.name()));
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
