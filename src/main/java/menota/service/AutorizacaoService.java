package menota.service;

import menota.model.Profissional;
import menota.model.Usuario;
import menota.repository.AutorizacaoRepository;
import menota.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;

@Service
public class AutorizacaoService {

    private final AutorizacaoRepository autorizacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public AutorizacaoService(
            AutorizacaoRepository autorizacaoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.autorizacaoRepository = autorizacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Profissional> listarAutorizados(String email) {
        Usuario paciente = buscarPaciente(email);

        return autorizacaoRepository.listarAutorizados(paciente.id());
    }

    public void autorizar(
            String emailPaciente,
            String emailProfissional
    ) {
        Usuario paciente = buscarPaciente(emailPaciente);

        if (emailProfissional == null || emailProfissional.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o e-mail do profissional."
            );
        }

        String email = emailProfissional.trim().toLowerCase(Locale.ROOT);

        Usuario profissional = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Profissional não encontrado."
                ));

        if (!profissional.perfil().equals("PROFISSIONAL")) {
            throw new IllegalArgumentException(
                    "O e-mail não pertence a uma conta profissional."
            );
        }

        autorizacaoRepository.autorizar(
                paciente.id(),
                profissional.id()
        );
    }

    public void revogar(String emailPaciente, Long profissionalId) {
        Usuario paciente = buscarPaciente(emailPaciente);

        autorizacaoRepository.revogar(paciente.id(), profissionalId);
    }

    private Usuario buscarPaciente(String email) {
        Usuario paciente = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Conta não encontrada."
                ));

        if (!paciente.perfil().equals("PACIENTE")) {
            throw new AccessDeniedException(
                    "Esta operação é exclusiva do paciente."
            );
        }

        return paciente;
    }

}