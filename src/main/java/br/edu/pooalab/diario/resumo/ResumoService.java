package br.edu.pooalab.diario.resumo;

import br.edu.pooalab.diario.registro.*;
import br.edu.pooalab.diario.usuario.*;
import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumoService {
    private final RegistroRepository registros;
    private final ResumoRepository resumos;
    private final UsuarioRepository usuarios;
    private final CriptografiaTexto criptografia;
    private final Sumarizador sumarizador;
    private final ObjectMapper json;
    private final TransactionTemplate transacao;
    private final BemEstarCriptografia bemEstar;
    private final Set<String> emProcessamento = ConcurrentHashMap.newKeySet();

    public ResumoService(RegistroRepository registros, ResumoRepository resumos, UsuarioRepository usuarios,
            CriptografiaTexto criptografia, Sumarizador sumarizador, ObjectMapper json, PlatformTransactionManager manager, BemEstarCriptografia bemEstar) {
        this.registros = registros; this.resumos = resumos; this.usuarios = usuarios;
        this.criptografia = criptografia; this.sumarizador = sumarizador; this.json = json;
        this.transacao = new TransactionTemplate(manager);
        this.bemEstar = bemEstar;
    }

    public record Fonte(LocalDate data, long registroId, long versao) {}
    public record Item(UUID id, LocalDate inicio, LocalDate fim, Instant criadoEm, String modelo) {}
    public record Detalhe(UUID id, LocalDate inicio, LocalDate fim, Instant criadoEm, String texto,
                          String modelo, String configuracao, List<LocalDate> datasUtilizadas, List<Sumarizador.Emocao> emocoes) {}
    public record Geracao(Detalhe resumo, boolean reutilizado) {}
    private record Contexto(List<Fonte> fontes, List<Sumarizador.Entrada> entradas, String assinatura) {}

    public boolean configurado() { return sumarizador.configurado(); }
    public String modelo() { return sumarizador.configuracao().modelo(); }

    public List<Item> listar(String login) {
        return transacao.execute(tx -> resumos.findTop20ByUsuarioLoginOrderByCriadoEmDesc(login).stream()
            .map(r -> new Item(r.getId(), r.getInicio(), r.getFim(), r.getCriadoEm(), r.getModelo())).toList());
    }

    public Detalhe consultar(String login, UUID id) {
        return transacao.execute(tx -> detalhe(resumos.findByIdAndUsuarioLogin(id, login)
            .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Resumo não encontrado. Ele pode ter sido removido após uma alteração no diário."))));
    }

    public Geracao gerar(String login, LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio) || ChronoUnit.DAYS.between(inicio, fim) > 30)
            throw erro(HttpStatus.BAD_REQUEST, "Escolha um período de até 31 dias, com início anterior ou igual ao fim.");
        if (!emProcessamento.add(login)) throw erro(HttpStatus.CONFLICT, "Já existe um resumo sendo gerado para sua conta. Aguarde.");
        try {
            // Transações curtas: não mantém o banco bloqueado enquanto espera a API externa.
            var configuracao = sumarizador.configuracao();
            Contexto contexto = transacao.execute(tx -> contexto(login, inicio, fim, configuracao));
            var existente = transacao.execute(tx -> resumos.findByUsuarioLoginAndInicioAndFimAndAssinatura(login, inicio, fim, contexto.assinatura())
                .map(this::detalhe).orElse(null));
            if (existente != null) return new Geracao(existente, true);
            var resultado = sumarizador.resumir(contexto.entradas());
            return transacao.execute(tx -> {
                var usuario = usuarios.buscarParaEscrita(login).orElseThrow();
                var atual = contexto(login, inicio, fim, configuracao);
                if (!contexto.assinatura().equals(atual.assinatura()))
                    throw erro(HttpStatus.CONFLICT, "O diário mudou durante a geração. O resumo não foi salvo; confira o período e tente novamente.");
                var resumo = new Resumo(usuario, inicio, fim, criptografia.cifrar(resultado.texto()),
                    serializar(contexto.fontes()), resultado.modelo(), configuracao.versao(), contexto.assinatura());
                resumo.setEmocoesCifradas(criptografia.cifrar(serializar(resultado.emocoes())));
                return new Geracao(detalhe(resumos.saveAndFlush(resumo)), false);
            });
        } finally { emProcessamento.remove(login); }
    }

    private Contexto contexto(String login, LocalDate inicio, LocalDate fim, Sumarizador.Configuracao configuracao) {
        usuarios.buscarParaEscrita(login).orElseThrow();
        var selecionados = registros.findByUsuarioLoginAndDataBetweenOrderByDataAsc(login, inicio, fim);
        if (selecionados.isEmpty()) throw erro(HttpStatus.UNPROCESSABLE_ENTITY, "Não há registros salvos neste período.");
        var entradas = selecionados.stream().map(r -> new Sumarizador.Entrada(r.getData(), criptografia.decifrar(r.getTextoCifrado()), bemEstar.ler(r.getBemEstarCifrado()))).toList();
        int tamanho = entradas.stream().mapToInt(e -> e.texto().length()).sum();
        if (tamanho > 24000) throw erro(HttpStatus.PAYLOAD_TOO_LARGE,
            "O período ultrapassa 24.000 caracteres. Escolha menos dias; nenhum registro foi cortado ou enviado.");
        if (entradas.stream().mapToInt(e -> e.texto().strip().length()).sum() < 30)
            throw erro(HttpStatus.UNPROCESSABLE_ENTITY, "Há pouco conteúdo para resumir. Selecione registros com pelo menos 30 caracteres ao todo.");
        var fontes = selecionados.stream().map(r -> new Fonte(r.getData(), r.getId(), r.getVersao())).toList();
        String assinatura = hash(inicio + "/" + fim + "/" + configuracao.modelo() + "/" + configuracao.versao() + "/" + serializar(fontes));
        return new Contexto(fontes, entradas, assinatura);
    }

    private Detalhe detalhe(Resumo resumo) {
        try {
            List<Fonte> fontes = json.readValue(resumo.getFontesJson(), new TypeReference<>() {});
            return new Detalhe(resumo.getId(), resumo.getInicio(), resumo.getFim(), resumo.getCriadoEm(),
                criptografia.decifrar(resumo.getTextoCifrado()), resumo.getModelo(), resumo.getConfiguracao(),
                fontes.stream().map(Fonte::data).toList(), resumo.getEmocoesCifradas() == null ? List.of() :
                    json.readValue(criptografia.decifrar(resumo.getEmocoesCifradas()), new TypeReference<List<Sumarizador.Emocao>>() {}));
        } catch (java.io.IOException e) { throw new IllegalStateException("Metadados do resumo inválidos."); }
    }

    private String serializar(Object objeto) {
        try { return json.writeValueAsString(objeto); }
        catch (java.io.IOException e) { throw new IllegalStateException("Não foi possível preparar o resumo."); }
    }
    private String hash(String dados) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 indisponível."); }
    }
    private ResponseStatusException erro(HttpStatus status, String mensagem) { return new ResponseStatusException(status, mensagem); }
}
