package br.edu.pooalab.diario.seguranca;

import br.edu.pooalab.diario.usuario.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;

@Configuration
public class SegurancaConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return login -> usuarios.findByLogin(login)
                .map(u -> User.withUsername(u.getLogin()).password(u.getSenhaHash()).roles(u.getPerfil()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/cadastro", "/preview", "/css/**", "/js/**", "/images/**", "/fonts/**", "/error").permitAll()
                .requestMatchers("/api/registros", "/api/registros/**", "/api/resumos", "/api/resumos/**").hasRole("PACIENTE")
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?saiu"))
            .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> {
                if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                    response.setStatus(401);
                } else {
                    response.sendRedirect(request.getContextPath() + "/login");
                }
            }))
            .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'")));
        // Sessão e CSRF padrão do Spring: não há JWT nem credenciais no JavaScript.
        return http.build();
    }
}
