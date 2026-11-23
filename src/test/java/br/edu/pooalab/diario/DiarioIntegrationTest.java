package br.edu.pooalab.diario;

import br.edu.pooalab.diario.usuario.UsuarioService;
import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiarioIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarios;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate banco;
    @Autowired CriptografiaTexto criptografia;
    private static final String SENHA = "SenhaFicticia123!";

    @BeforeEach
    void preparar() {
        usuarios.cadastrar("aluno_a", SENHA);
        usuarios.cadastrar("aluno_b", SENHA);
    }

    private ResultActions salvar(String login, String data, String texto, long versao) throws Exception {
        return mvc.perform(put("/api/registros").with(user(login).roles("PACIENTE")).with(csrf())
            .header("X-Diario-Usuario", login).contentType("application/json")
            .content(json.writeValueAsString(Map.of("data", data, "texto", texto, "versao", versao))));
    }

    private ResultActions buscar(String login, String data) throws Exception {
        return mvc.perform(get("/api/registros").with(user(login).roles("PACIENTE"))
            .header("X-Diario-Usuario", login).param("data", data));
    }

    @Test void cadastroLoginLogoutESenhaProtegida() throws Exception {
        mvc.perform(post("/cadastro").with(csrf()).param("login", "novo_aluno").param("senha", SENHA))
            .andExpect(redirectedUrl("/login?cadastro"));
        String hash = banco.queryForObject("select senha_hash from usuarios where login='novo_aluno'", String.class);
        assertThat(hash).startsWith("$2a$12$").doesNotContain(SENHA);
        var resultado = mvc.perform(post("/login").with(csrf()).param("username", "novo_aluno").param("password", SENHA))
            .andExpect(authenticated().withUsername("novo_aluno")).andExpect(redirectedUrl("/"))
            .andReturn();
        var sessao = (MockHttpSession) resultado.getRequest().getSession(false);
        mvc.perform(get("/").session(sessao)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Como foi seu dia?")));
        mvc.perform(post("/logout").session(sessao).with(csrf())).andExpect(unauthenticated());
        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test void marcaEFontesPublicasMantemApiProtegida() throws Exception {
        mvc.perform(get("/images/menota-logo.png")).andExpect(status().isOk())
            .andExpect(content().contentType("image/png"));
        mvc.perform(get("/images/menota-icon.svg")).andExpect(status().isOk());
        mvc.perform(get("/fonts/Caveat-Variable.ttf")).andExpect(status().isOk());
        mvc.perform(get("/fonts/Nunito-Variable.ttf")).andExpect(status().isOk());
        mvc.perform(get("/api/registros").param("data", "2026-10-29"))
            .andExpect(status().isUnauthorized());
    }

    @Test void rejeitaSenhaIncorreta() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "aluno_a").param("password", "incorreta"))
            .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test void bloqueiaAcessoAnonimoECsrf() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/registros").param("data", "2026-10-27")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/registros").with(user("aluno_a").roles("PACIENTE"))).andExpect(status().isForbidden());
    }

    @Test void criaEditaRecuperaEExclui() throws Exception {
        salvar("aluno_a", "2026-10-27", "Hoje apresentei meu trabalho fictício.", -1).andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(0));
        buscar("aluno_a", "2026-10-27").andExpect(jsonPath("$.texto").value("Hoje apresentei meu trabalho fictício."));
        salvar("aluno_a", "2026-10-27", "Editei meu relato.", 0).andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1));
        buscar("aluno_a", "2026-10-27").andExpect(jsonPath("$.texto").value("Editei meu relato."));
        mvc.perform(delete("/api/registros").with(user("aluno_a").roles("PACIENTE")).with(csrf()).header("X-Diario-Usuario", "aluno_a")
            .param("data", "2026-10-27").param("versao", "1")).andExpect(status().isNoContent());
        buscar("aluno_a", "2026-10-27").andExpect(jsonPath("$.versao").value(-1)).andExpect(jsonPath("$.texto").value(""));
    }

    @Test void isolaUsuariosMesmoComParametrosAdulterados() throws Exception {
        salvar("aluno_a", "2026-10-27", "Relato privado fictício de A.", -1).andExpect(status().isOk());
        mvc.perform(get("/api/registros").with(user("aluno_b").roles("PACIENTE")).header("X-Diario-Usuario", "aluno_b")
            .param("data", "2026-10-27").param("usuario", "aluno_a").param("id", "1"))
            .andExpect(jsonPath("$.texto").value(""));
        salvar("aluno_b", "2026-10-27", "Registro de B.", -1).andExpect(status().isOk());
        mvc.perform(delete("/api/registros").with(user("aluno_b").roles("PACIENTE")).with(csrf()).header("X-Diario-Usuario", "aluno_b")
            .param("data", "2026-10-27").param("versao", "0").param("usuario", "aluno_a").param("id", "1"))
            .andExpect(status().isNoContent());
        buscar("aluno_a", "2026-10-27").andExpect(jsonPath("$.texto").value("Relato privado fictício de A."));
    }

    @Test void impedeEnviarRascunhoAContaTrocadaEmOutraAba() throws Exception {
        mvc.perform(put("/api/registros").with(user("aluno_b").roles("PACIENTE")).with(csrf()).header("X-Diario-Usuario", "aluno_a")
            .contentType("application/json").content("{\"data\":\"2026-10-27\",\"texto\":\"privado\",\"versao\":-1}"))
            .andExpect(status().isConflict());
        buscar("aluno_b", "2026-10-27").andExpect(jsonPath("$.texto").value(""));
    }

    @Test void datasIndependentesESemDuplicacoes() throws Exception {
        salvar("aluno_a", "2026-10-26", "Dia anterior.", -1).andExpect(status().isOk());
        salvar("aluno_a", "2026-10-27", "Dia atual.", -1).andExpect(status().isOk());
        salvar("aluno_a", "2026-10-27", "Tentativa duplicada.", -1).andExpect(status().isConflict());
        buscar("aluno_a", "2026-10-26").andExpect(jsonPath("$.texto").value("Dia anterior."));
        buscar("aluno_a", "2026-10-27").andExpect(jsonPath("$.texto").value("Dia atual."));
        assertThat(banco.queryForObject("select count(*) from registros", Long.class)).isEqualTo(2);
    }

    @Test void conflitoNaoSobrescreveNemExcluiVersaoAtual() throws Exception {
        salvar("aluno_a", "2026-10-27", "Primeira versão.", -1).andExpect(status().isOk());
        salvar("aluno_a", "2026-10-27", "Versão atual.", 0).andExpect(status().isOk());
        salvar("aluno_a", "2026-10-27", "Versão atrasada.", 0).andExpect(status().isConflict());
        mvc.perform(delete("/api/registros").with(user("aluno_a").roles("PACIENTE")).with(csrf()).header("X-Diario-Usuario", "aluno_a")
            .param("data", "2026-10-27").param("versao", "0")).andExpect(status().isConflict());
        buscar("aluno_a", "2026-10-27").andExpect(jsonPath("$.texto").value("Versão atual."));
    }

    @Test void rejeitaEntradasInvalidasSemSalvar() throws Exception {
        salvar("aluno_a", "2026-10-27", "   ", -1).andExpect(status().isBadRequest());
        salvar("aluno_a", "2026-10-27", "a".repeat(20001), -1).andExpect(status().isBadRequest());
        buscar("aluno_a", "data-invalida").andExpect(status().isBadRequest());
        assertThat(banco.queryForObject("select count(*) from registros", Long.class)).isZero();
    }

    @Test void textoArmazenadoEstaCifradoComIvAleatorio() throws Exception {
        String relato = "Eu não estava triste; tenho dúvidas sobre amanhã.";
        salvar("aluno_a", "2026-10-27", relato, -1).andExpect(status().isOk());
        String cifrado = banco.queryForObject("select texto_cifrado from registros", String.class);
        assertThat(cifrado).doesNotContain(relato);
        assertThat(criptografia.decifrar(cifrado)).isEqualTo(relato);
        assertThat(criptografia.cifrar(relato)).isNotEqualTo(cifrado);
    }

    @Test void dadosPessoaisNaoSaoCacheados() throws Exception {
        buscar("aluno_a", "2026-10-27").andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test void cadastroRejeitaLoginInvalidoESenhaCurta() throws Exception {
        mvc.perform(post("/cadastro").with(csrf()).param("login", "INVALIDO").param("senha", "curta"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("cadastro", "login", "senha"));
    }
}
