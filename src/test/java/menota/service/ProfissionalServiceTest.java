package menota.service;

import menota.model.Nota;
import menota.model.Resumo;
import menota.model.Usuario;
import menota.repository.AutorizacaoRepository;
import menota.repository.NotaRepository;
import menota.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProfissionalServiceTest {

    private final UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    private final AutorizacaoRepository autorizacaoRepository =
            mock(AutorizacaoRepository.class);

    private final NotaRepository notaRepository =
            mock(NotaRepository.class);

    private final ResumoService resumoService =
            mock(ResumoService.class);

    private final ProfissionalService service = new ProfissionalService(
            usuarioRepository,
            autorizacaoRepository,
            notaRepository,
            resumoService
    );

    private final Usuario profissional = new Usuario(
            2L,
            "Profissional Teste",
            "profissional@menota.test",
            "hash-ficticio",
            "PROFISSIONAL"
    );

    private final Usuario paciente = new Usuario(
            1L,
            "Paciente Teste",
            "paciente@menota.test",
            "hash-ficticio",
            "PACIENTE"
    );

    @BeforeEach
    void prepararUsuarios() {
        when(usuarioRepository.buscarPorEmail(profissional.email()))
                .thenReturn(Optional.of(profissional));

        when(usuarioRepository.buscarPorId(paciente.id()))
                .thenReturn(Optional.of(paciente));
    }

    @Test
    void deveBloquearConsultaSemAutorizacao() {
        when(autorizacaoRepository.possuiAcesso(
                paciente.id(),
                profissional.id()
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.listarNotas(
                        profissional.email(),
                        paciente.id()
                )
        );

        verifyNoInteractions(notaRepository, resumoService);
    }

    @Test
    void deveBloquearResumoSemAutorizacao() {
        when(autorizacaoRepository.possuiAcesso(
                paciente.id(),
                profissional.id()
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.gerarResumo(
                        profissional.email(),
                        paciente.id(),
                        7
                )
        );

        verifyNoInteractions(notaRepository, resumoService);
    }

    @Test
    void deveConsultarNotasDoPacienteAutorizado() {
        when(autorizacaoRepository.possuiAcesso(
                paciente.id(),
                profissional.id()
        )).thenReturn(true);

        List<Nota> notas = List.of(
                new Nota(
                        10L,
                        LocalDate.of(2026, 11, 21),
                        "Relato fictício para o teste."
                )
        );

        when(notaRepository.listarPorPaciente(paciente.id()))
                .thenReturn(notas);

        List<Nota> resultado = service.listarNotas(
                profissional.email(),
                paciente.id()
        );

        assertEquals(notas, resultado);

        verify(notaRepository).listarPorPaciente(paciente.id());
    }

    @Test
    void deveBloquearResultadoSeAcessoForRevogadoDuranteResumo() {
        when(autorizacaoRepository.possuiAcesso(
                paciente.id(),
                profissional.id()
        )).thenReturn(true, false);

        Resumo resumo = new Resumo(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 21),
                "Resumo fictício."
        );

        when(resumoService.gerar(paciente.email(), 7))
                .thenReturn(resumo);

        assertThrows(
                AccessDeniedException.class,
                () -> service.gerarResumo(
                        profissional.email(),
                        paciente.id(),
                        7
                )
        );

        verify(resumoService).gerar(paciente.email(), 7);
    }
}