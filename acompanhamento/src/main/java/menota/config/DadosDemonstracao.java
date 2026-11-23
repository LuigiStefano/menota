package menota.config;

import menota.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class DadosDemonstracao implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public DadosDemonstracao(
            UsuarioRepository repository,
            PasswordEncoder encoder
    ) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        repository.inserirSeAusente(
                "Paciente Teste",
                "paciente@menota.test",
                encoder.encode("Paciente123!"),
                "PACIENTE"
        );

        repository.inserirSeAusente(
                "Profissional Teste",
                "profissional@menota.test",
                encoder.encode("Profissional123!"),
                "PROFISSIONAL"
        );
    }
}