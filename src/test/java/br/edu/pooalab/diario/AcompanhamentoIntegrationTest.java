package br.edu.pooalab.diario;

import br.edu.pooalab.diario.usuario.UsuarioService;
import br.edu.pooalab.diario.registro.RegistroService;
import br.edu.pooalab.diario.resumo.*;
import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class AcompanhamentoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarios;
    @Autowired RegistroService registros;
    @Autowired ResumoService resumos;
    @Autowired JdbcTemplate banco;
    @Autowired CriptografiaTexto criptografia;
    @Autowired ObjectMapper json;
    @MockitoBean Sumarizador sumarizador;
    static final LocalDate DIA = LocalDate.of(2026,10,29);

    @Test void profissionalTemSomenteConsultaNoAcompanhamento() throws Exception {
        mvc.perform(get("/api/registros").with(user("prof_t").roles("PROFISSIONAL"))
                .header("X-Diario-Usuario", "prof_t").param("data", DIA.toString()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/resumos").with(user("prof_t").roles("PROFISSIONAL"))
                .with(csrf()).header("X-Diario-Usuario", "prof_t"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/resumos").with(user("prof_t").roles("PROFISSIONAL")))
            .andExpect(redirectedUrl("/profissional"));
    }
    @BeforeEach void preparar() {
        when(sumarizador.configuracao()).thenReturn(new Sumarizador.Configuracao("modelo-teste", "configuracao-teste"));
        usuarios.cadastrar("paciente_t", "SenhaFicticia123!");
        usuarios.cadastrar("outro_t", "SenhaFicticia123!");
        usuarios.cadastrar("prof_t", "SenhaFicticia123!", "PROFISSIONAL");
        usuarios.cadastrar("prof_b", "SenhaFicticia123!", "PROFISSIONAL");
        registros.salvar("paciente_t", DIA, "Hoje fiquei alegre ao terminar uma atividade em grupo.", -1);
        registros.salvar("outro_t", DIA, "Outro relato privado que não pode aparecer para o profissional.", -1);
    }
    ResultActions autorizar(String conta, String destino, boolean confirma) throws Exception {
        return mvc.perform(post("/api/acompanhamento/acessos").with(user(conta)).with(csrf()).header("X-Diario-Usuario",conta)
            .contentType("application/json").content(json.writeValueAsString(Map.of("profissional",destino,"autorizo",confirma))));
    }
    ResultActions consultar(String profissional, String paciente) throws Exception {
        return mvc.perform(get("/api/acompanhamento/pacientes/"+paciente).with(user(profissional)).header("X-Diario-Usuario",profissional)
            .param("inicio",DIA.toString()).param("fim",DIA.toString()));
    }
    @Test void exigeConsentimentoETipoDeConta() throws Exception {
        autorizar("paciente_t","prof_t",false).andExpect(status().isBadRequest());
        autorizar("paciente_t","outro_t",true).andExpect(status().isNotFound());
        autorizar("prof_t","prof_b",true).andExpect(status().isForbidden());
        mvc.perform(post("/api/acompanhamento/acessos").with(user("paciente_t"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/acompanhamento/pacientes")).andExpect(status().isUnauthorized());
    }
    @Test void naoLeSemAutorizacaoENaoTrocaPaciente() throws Exception {
        consultar("prof_t","paciente_t").andExpect(status().isNotFound());
        autorizar("paciente_t","prof_t",true).andExpect(status().isNoContent());
        consultar("prof_t","paciente_t").andExpect(status().isOk()).andExpect(jsonPath("$.registros.length()").value(1))
            .andExpect(jsonPath("$.registros[0].texto").value("Hoje fiquei alegre ao terminar uma atividade em grupo."))
            .andExpect(header().string("Cache-Control","no-store"));
        consultar("prof_t","outro_t").andExpect(status().isNotFound());
        consultar("prof_b","paciente_t").andExpect(status().isNotFound());
        consultar("outro_t","paciente_t").andExpect(status().isForbidden());
    }
    @Test void revogacaoBloqueiaNovasConsultas() throws Exception {
        autorizar("paciente_t","prof_t",true).andExpect(status().isNoContent());
        mvc.perform(delete("/api/acompanhamento/acessos/prof_t").with(user("paciente_t")).with(csrf()).header("X-Diario-Usuario","paciente_t"))
            .andExpect(status().isNoContent());
        consultar("prof_t","paciente_t").andExpect(status().isNotFound());
        mvc.perform(get("/api/acompanhamento/pacientes").with(user("prof_t")).header("X-Diario-Usuario","prof_t"))
            .andExpect(jsonPath("$.length()").value(0));
    }
    @Test void protegeContaDaAbaELimiteDoPeriodo() throws Exception {
        autorizar("paciente_t","prof_t",true).andExpect(status().isNoContent());
        mvc.perform(get("/api/acompanhamento/pacientes/paciente_t").with(user("prof_t")).header("X-Diario-Usuario","prof_b")
            .param("inicio",DIA.toString()).param("fim",DIA.toString())).andExpect(status().isConflict());
        mvc.perform(get("/api/acompanhamento/pacientes/paciente_t").with(user("prof_t")).header("X-Diario-Usuario","prof_t")
            .param("inicio",DIA.minusDays(31).toString()).param("fim",DIA.toString())).andExpect(status().isBadRequest());
    }
    @Test void compartilhaEmocoesCifradasSemPermitirEscrita() throws Exception {
        when(sumarizador.resumir(anyList())).thenReturn(new Sumarizador.Resultado("Relatou alegria ao terminar uma atividade.","modelo-teste",
            List.of(new Sumarizador.Emocao(DIA,"alegria","fiquei alegre",false))));
        resumos.gerar("paciente_t",DIA,DIA);
        String cifrado = banco.queryForObject("select emocoes_cifradas from resumos where usuario_id=(select id from usuarios where login='paciente_t')",String.class);
        assertThat(cifrado).doesNotContain("alegria");
        assertThat(criptografia.decifrar(cifrado)).contains("fiquei alegre");
        autorizar("paciente_t","prof_t",true).andExpect(status().isNoContent());
        consultar("prof_t","paciente_t").andExpect(jsonPath("$.analises[0].emocoes[0].nome").value("alegria"));
        mvc.perform(put("/api/acompanhamento/pacientes/paciente_t").with(user("prof_t")).with(csrf()).header("X-Diario-Usuario","prof_t"))
            .andExpect(status().isMethodNotAllowed());
    }
    @Test void cadastroProfissionalEDirecionamento() throws Exception {
        mvc.perform(post("/cadastro").with(csrf()).param("login","novo_prof").param("senha","SenhaFicticia123!").param("perfil","PROFISSIONAL"))
            .andExpect(redirectedUrl("/login?cadastro"));
        mvc.perform(get("/").with(user("novo_prof"))).andExpect(redirectedUrl("/profissional"));
        mvc.perform(get("/profissional").with(user("paciente_t"))).andExpect(redirectedUrl("/"));
    }
}
