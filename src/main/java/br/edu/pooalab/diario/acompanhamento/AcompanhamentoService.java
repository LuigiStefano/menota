package br.edu.pooalab.diario.acompanhamento;

import br.edu.pooalab.diario.usuario.*;
import br.edu.pooalab.diario.registro.*;
import br.edu.pooalab.diario.resumo.ResumoService;
import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

// A autorização é conferida no servidor em toda consulta, inclusive após uma revogação.
@Service
@Transactional
public class AcompanhamentoService {
    private final UsuarioRepository usuarios;
    private final RegistroRepository registros;
    private final ResumoService resumos;
    private final CriptografiaTexto criptografia;
    private final AcessoRepository acessos;
    private final BemEstarCriptografia bemEstar;

    public AcompanhamentoService(UsuarioRepository usuarios, RegistroRepository registros,
            ResumoService resumos, CriptografiaTexto criptografia, AcessoRepository acessos, BemEstarCriptografia bemEstar) {
        this.usuarios = usuarios; this.registros = registros; this.resumos = resumos;
        this.criptografia = criptografia; this.acessos = acessos; this.bemEstar = bemEstar;
    }
    public record Conta(String login, Instant desde) {}
    public record Pagina(LocalDate data, String texto, BemEstar bemEstar) {}
    public record Periodo(String paciente, List<Pagina> registros, List<ResumoService.Detalhe> analises) {}

    public List<Conta> acessos(String paciente) {
        var conta = paciente(paciente);
        return acessos.profissionaisDo(conta.getId()).stream()
            .map(acesso -> new Conta(acesso.login(), acesso.desde())).toList();
    }

    public void autorizar(String login, String profissional) {
        var paciente = paciente(login);
        var destino = usuarios.findByLogin(profissional).filter(Usuario::isProfissional)
            .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Confira o usuário do profissional."));
        acessos.autorizar(paciente.getId(), destino.getId(), Instant.now());
    }

    public void revogar(String login, String profissional) {
        var paciente = paciente(login);
        acessos.revogar(paciente.getId(), profissional);
    }

    public List<Conta> pacientes(String login) {
        var profissional = profissional(login);
        return acessos.pacientesDo(profissional.getId()).stream()
            .map(acesso -> new Conta(acesso.login(), acesso.desde())).toList();
    }

    public Periodo consultar(String login, String paciente, LocalDate inicio, LocalDate fim) {
        var profissional = profissional(login);
        if (fim.isBefore(inicio) || ChronoUnit.DAYS.between(inicio, fim) > 30)
            throw erro(HttpStatus.BAD_REQUEST, "Escolha um período de até 31 dias.");
        // O mesmo bloqueio usado na revogação impede leituras iniciadas após ela.
        var titular = usuarios.buscarParaEscrita(paciente)
            .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Acompanhamento não disponível."));
        if (!acessos.temAcesso(titular.getId(), profissional.getId()))
            throw erro(HttpStatus.NOT_FOUND, "Acompanhamento não disponível.");
        var paginas = registros.findByUsuarioLoginAndDataBetweenOrderByDataAsc(paciente, inicio, fim).stream()
            .map(r -> new Pagina(r.getData(), criptografia.decifrar(r.getTextoCifrado()), bemEstar.ler(r.getBemEstarCifrado()))).toList();
        var analises = resumos.listar(paciente).stream()
            .filter(r -> !r.inicio().isBefore(inicio) && !r.fim().isAfter(fim))
            .map(r -> resumos.consultar(paciente, r.id())).toList();
        return new Periodo(paciente, paginas, analises);
    }

    private Usuario paciente(String login) {
        var usuario = usuarios.buscarParaEscrita(login).orElseThrow();
        if (usuario.isProfissional()) throw erro(HttpStatus.FORBIDDEN, "Esta ação é da área do paciente.");
        return usuario;
    }
    private Usuario profissional(String login) {
        return usuarios.findByLogin(login).filter(Usuario::isProfissional)
            .orElseThrow(() -> erro(HttpStatus.FORBIDDEN, "Entre com uma conta de profissional."));
    }
    private ResponseStatusException erro(HttpStatus status, String texto) { return new ResponseStatusException(status, texto); }
}
