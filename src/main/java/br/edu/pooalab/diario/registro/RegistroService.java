package br.edu.pooalab.diario.registro;

import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import br.edu.pooalab.diario.usuario.UsuarioRepository;
import br.edu.pooalab.diario.resumo.ResumoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

@Service
public class RegistroService {
    private final RegistroRepository registros;
    private final UsuarioRepository usuarios;
    private final CriptografiaTexto criptografia;
    private final ResumoRepository resumos;

    public RegistroService(RegistroRepository registros, UsuarioRepository usuarios, CriptografiaTexto criptografia, ResumoRepository resumos) {
        this.registros = registros;
        this.usuarios = usuarios;
        this.criptografia = criptografia;
        this.resumos = resumos;
    }

    public record Pagina(LocalDate data, String texto, long versao) {}

    @Transactional(readOnly = true)
    public Pagina buscar(String login, LocalDate data) {
        return registros.findByUsuarioLoginAndData(login, data)
            .map(this::pagina).orElse(new Pagina(data, "", -1));
    }

    @Transactional
    public Pagina salvar(String login, LocalDate data, String texto, long versao) {
        var usuario = usuarios.buscarParaEscrita(login).orElseThrow();
        var registro = registros.findByUsuarioLoginAndData(login, data).orElse(null);
        conferirVersao(registro, versao);
        if (registro == null) registro = new Registro(usuario, data);
        registro.setTextoCifrado(criptografia.cifrar(texto));
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
        return new Pagina(registro.getData(), criptografia.decifrar(registro.getTextoCifrado()), registro.getVersao());
    }

    private void conferirVersao(Registro registro, long versao) {
        long atual = registro == null ? -1 : registro.getVersao();
        if (versao != atual) throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Este registro mudou em outra aba. Preserve seu texto e reabra a data antes de tentar novamente.");
    }
}
