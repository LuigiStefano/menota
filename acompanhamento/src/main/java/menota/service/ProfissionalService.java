package menota.service;

import menota.model.Nota;
import menota.model.Paciente;
import menota.model.Resumo;
import menota.model.Usuario;
import menota.repository.AutorizacaoRepository;
import menota.repository.NotaRepository;
import menota.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfissionalService {

    private final UsuarioRepository usuarioRepository;
    private final AutorizacaoRepository autorizacaoRepository;
    private final NotaRepository notaRepository;
    private final ResumoService resumoService;

    public ProfissionalService(
            UsuarioRepository usuarioRepository,
            AutorizacaoRepository autorizacaoRepository,
            NotaRepository notaRepository,
            ResumoService resumoService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.notaRepository = notaRepository;
        this.resumoService = resumoService;
    }

    public List<Paciente> listarPacientes(String email) {
        Usuario profissional = buscarProfissional(email);

        return autorizacaoRepository.listarPacientesAutorizados(
                profissional.id()
        );
    }

    public Paciente buscarPacienteAutorizado(
            String email,
            Long pacienteId
    ) {
        Usuario profissional = buscarProfissional(email);

        if (!autorizacaoRepository.possuiAcesso(
                pacienteId,
                profissional.id()
        )) {
            throw new AccessDeniedException(
                    "Você não possui autorização para esse paciente."
            );
        }

        Usuario paciente = usuarioRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Paciente não disponível."
                ));

        if (!paciente.perfil().equals("PACIENTE")) {
            throw new AccessDeniedException(
                    "Paciente não disponível."
            );
        }

        return new Paciente(
                paciente.id(),
                paciente.nome(),
                paciente.email()
        );
    }

    public List<Nota> listarNotas(String email, Long pacienteId) {
        Paciente paciente = buscarPacienteAutorizado(email, pacienteId);

        return notaRepository.listarPorPaciente(paciente.id());
    }

    public Resumo gerarResumo(
            String email,
            Long pacienteId,
            int dias
    ) {
        Paciente paciente = buscarPacienteAutorizado(email, pacienteId);

        Resumo resumo = resumoService.gerar(paciente.email(), dias);

        buscarPacienteAutorizado(email, pacienteId);

        return resumo;
    }

    private Usuario buscarProfissional(String email) {
        Usuario profissional = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Conta não encontrada."
                ));

        if (!profissional.perfil().equals("PROFISSIONAL")) {
            throw new AccessDeniedException(
                    "Esta operação é exclusiva do profissional."
            );
        }

        return profissional;
    }
}