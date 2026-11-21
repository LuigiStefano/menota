package menota.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http.authorizeHttpRequests(acesso -> acesso
                .requestMatchers(
                        "/login",
                        "/css/**",
                        "/fonts/**",
                        "/images/**",
                        "/error"
                ).permitAll()
                .requestMatchers(
                        "/paciente",
                        "/paciente/**"
                ).hasRole("PACIENTE")
                .requestMatchers(
                        "/profissional",
                        "/profissional/**"
                ).hasRole("PROFISSIONAL")
                .anyRequest().authenticated()
        );

        http.formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
        );

        return http.build();
    }
}