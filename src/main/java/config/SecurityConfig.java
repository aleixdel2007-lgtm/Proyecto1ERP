package config;

import dao.UsuarioDao;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // Carga los usuarios desde la base de datos; el "username" del login es el email
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
                        .requestMatchers("/registro").permitAll()
                        .requestMatchers("/departamentos/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("CLIENTE", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // Los clientes van a la tienda; el resto de roles, a "/home"
                        .successHandler((request, response, authentication) -> {
                            boolean esCliente = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.CLIENTE.name()));
                            response.sendRedirect(request.getContextPath() + (esCliente ? "/tienda" : "/home"));
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                );

        return http.build();
    }
}