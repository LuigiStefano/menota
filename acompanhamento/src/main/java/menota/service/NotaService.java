package menota.service;

import menota.model.Nota;
import menota.model.Usuario;
import menota.repository.NotaRepository;
import menota.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    public NotaService(
            NotaRepository notaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
    }
    public void registrar(String email, String texto) {
        Usuario paciente = buscarPaciente(email);

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "Escreva algo antes de registrar."
            );
        }

        LocalDate hoje = LocalDate.now();

        notaRepository.salvar(paciente.id(), hoje, texto);
    }

    public List<Nota> listar(String email) {
        Usuario paciente = buscarPaciente(email);

        return notaRepository.listarPorPaciente(paciente.id());
    }

    private Usuario buscarPaciente(String email) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Conta não encontrada."
                ));

        if (!usuario.perfil().equals("PACIENTE")) {
            throw new AccessDeniedException(
                    "Esta operação é exclusiva do paciente."
            );
        }

        return usuario;
    }
}