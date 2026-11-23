package br.edu.pooalab.diario.registro;

import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import br.edu.pooalab.diario.usuario.UsuarioRepository;
import br.edu.pooalab.diario.resumo.ResumoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class RegistroService {
    private final RegistroRepository registros;
    private final UsuarioRepository usuarios;
    private final CriptografiaTexto criptografia;
    private final ResumoRepository resumos;
    private final BemEstarCriptografia bemEstar;

    public RegistroService(RegistroRepository registros, UsuarioRepository usuarios, CriptografiaTexto criptografia, ResumoRepository resumos, BemEstarCriptografia bemEstar) {
        this.registros = registros;
        this.usuarios = usuarios;
        this.criptografia = criptografia;
        this.resumos = resumos;
        this.bemEstar = bemEstar;
    }

    public record Pagina(LocalDate data, String texto, long versao, BemEstar bemEstar) {}

    @Transactional(readOnly = true)
    public List<LocalDate> datas(String login, YearMonth mes) {
        return registros.datasDoPeriodo(login, mes.atDay(1), mes.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public Pagina buscar(String login, LocalDate data) {
        return registros.findByUsuarioLoginAndData(login, data)
            .map(this::pagina).orElse(new Pagina(data, "", -1, BemEstar.vazio()));
    }

    @Transactional
    public Pagina salvar(String login, LocalDate data, String texto, long versao) {
        return salvar(login, data, texto, versao, null);
    }

    @Transactional
    public Pagina salvar(String login, LocalDate data, String texto, long versao, BemEstar informado) {
        var usuario = usuarios.buscarParaEscrita(login).orElseThrow();
        var registro = registros.findByUsuarioLoginAndData(login, data).orElse(null);
        conferirVersao(registro, versao);
        if (registro == null) registro = new Registro(usuario, data);
        registro.setTextoCifrado(criptografia.cifrar(texto));
        // Clientes antigos que omitem o campo preservam o autorrelato existente.
        if (informado != null) registro.setBemEstarCifrado(bemEstar.cifrar(informado));
        resumos.removerDoPeriodoAlterado(login, data);
        return pagina(registros.saveAndFlush(registro));
    }

    @Transactional
    public void excluir(String login, LocalDate data, long versao) {
        usuarios.buscarParaEscrita(login).orElseThrow();
        var registro = registros.findByUsuarioLoginAndData(login, data).orElse(null);
        conferirVersao(registro, versao);
        if (registro != null) {
            resumos.removerDoPeriodoAlterado(login, data);
            registros.delete(registro);
        }
    }

    private Pagina pagina(Registro registro) {
        return new Pagina(registro.getData(), criptografia.decifrar(registro.getTextoCifrado()), registro.getVersao(), bemEstar.ler(registro.getBemEstarCifrado()));
    }

    private void conferirVersao(Registro registro, long versao) {
        long atual = registro == null ? -1 : registro.getVersao();
        if (versao != atual) throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Este registro mudou em outra aba. Preserve seu texto e reabra a data antes de tentar novamente.");
    }
}
