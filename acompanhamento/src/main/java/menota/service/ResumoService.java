package menota.service;

import menota.model.Nota;
import menota.model.Resumo;
import menota.model.Usuario;
import menota.repository.NotaRepository;
import menota.repository.UsuarioRepository;
import menota.resumo.Sumarizador;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ResumoService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Sumarizador sumarizador;

    public ResumoService(
            NotaRepository notaRepository,
            UsuarioRepository usuarioRepository,
            Sumarizador sumarizador
    ) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
        this.sumarizador = sumarizador;
    }

    public Resumo gerar(String email, int dias) {
        if (dias != 7 && dias != 30) {
            throw new IllegalArgumentException(
                    "Escolha um período de 7 ou 30 dias."
            );
        }

        Usuario paciente = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Conta não encontrada."
                ));

        if (!paciente.perfil().equals("PACIENTE")) {
            throw new AccessDeniedException(
                    "Esta operação é exclusiva do paciente."
            );
        }

        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusDays(dias - 1);

        List<Nota> notas = notaRepository.listarPorPeriodo(
                paciente.id(),
                inicio,
                fim
        );

        if (notas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não existem anotações nesse período."
            );
        }

        StringBuilder relatos = new StringBuilder();

        for (Nota nota : notas) {
            relatos.append("Data: ");
            relatos.append(nota.data());
            relatos.append("\nRelato:\n");
            relatos.append(nota.texto());
            relatos.append("\n\n");
        }

        if (relatos.length() > 40000) {
            throw new IllegalArgumentException(
                    "O período reúne texto demais para este MVP. "
                            + "Tente um período menor."
            );
        }

        String texto = sumarizador.resumir(relatos.toString());

        return new Resumo(inicio, fim, texto);
    }
}