package br.edu.pooalab.diario;

import br.edu.pooalab.diario.registro.RegistroService;
import br.edu.pooalab.diario.resumo.*;
import br.edu.pooalab.diario.usuario.UsuarioService;
import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.transaction.annotation.Propagation;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ResumoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarios;
    @Autowired RegistroService registros;
    @Autowired JdbcTemplate banco;
    @Autowired ObjectMapper json;
    @Autowired CriptografiaTexto criptografia;
    @Autowired ResumoService servicoResumos;
    @MockitoBean Sumarizador sumarizador;
    static final String A = "resumo_a", B = "resumo_b";
    static final LocalDate DIA = LocalDate.of(2026, 10, 27);

    @BeforeEach void preparar() {
        usuarios.cadastrar(A, "SenhaFicticia123!"); usuarios.cadastrar(B, "SenhaFicticia123!");
        when(sumarizador.configurado()).thenReturn(true);
        when(sumarizador.resumir(anyList())).thenReturn(new Sumarizador.Resultado("Resumo fictício: concluiu uma atividade e relatou satisfação.", "modelo-de-teste"));
    }
    void escrever(String login, LocalDate dia, String texto) { registros.salvar(login,dia,texto,-1); }
    void exemplo() { escrever(A,DIA,"Relato fictício: concluí uma atividade e fiquei satisfeito com o resultado."); }
    ResultActions gerar(String login, String inicio, String fim, boolean consentimento) throws Exception {
        return mvc.perform(post("/api/resumos").with(user(login)).with(csrf()).header("X-Diario-Usuario",login)
            .contentType("application/json").content(json.writeValueAsString(Map.of("inicio",inicio,"fim",fim,"confirmoEnvioFicticio",consentimento))));
    }
    String gerarId() throws Exception {
        var r=gerar(A,DIA.toString(),DIA.toString(),true).andExpect(status().isOk()).andReturn();
        return json.readTree(r.getResponse().getContentAsString()).path("resumo").path("id").asText();
    }

    @Test void geraConsultaECifraResumo() throws Exception {
        exemplo(); String id=gerarId();
        mvc.perform(get("/api/resumos/"+id).with(user(A)).header("X-Diario-Usuario",A)).andExpect(status().isOk())
            .andExpect(jsonPath("$.datasUtilizadas[0]").value(DIA.toString())).andExpect(jsonPath("$.modelo").value("modelo-de-teste"));
        String cifrado=banco.queryForObject("select texto_cifrado from resumos",String.class);
        assertThat(cifrado).doesNotContain("Resumo fictício");
        assertThat(criptografia.decifrar(cifrado)).contains("concluiu uma atividade");
        mvc.perform(get("/api/resumos").with(user(A)).header("X-Diario-Usuario",A)).andExpect(jsonPath("$.length()").value(1));
    }
    @Test void reutilizaSemNovaChamada() throws Exception {
        exemplo(); String id=gerarId();
        gerar(A,DIA.toString(),DIA.toString(),true).andExpect(jsonPath("$.reutilizado").value(true)).andExpect(jsonPath("$.resumo.id").value(id));
        verify(sumarizador,times(1)).resumir(anyList());
        assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isEqualTo(1);
    }
    @Test void selecionaSomenteContaEPeriodoSemIdsNaEntradaDaIA() throws Exception {
        exemplo(); escrever(A,DIA.minusDays(1),"Texto fora do período que não deve ser enviado para a IA.");
        escrever(B,DIA,"Texto da outra conta que não deve ser enviado para a IA.");
        gerarId();
        verify(sumarizador).resumir(argThat(lista -> lista.size()==1 && lista.get(0).data().equals(DIA)
            && lista.get(0).texto().startsWith("Relato fictício:")));
    }
    @Test void outroUsuarioNaoConsultaResumoMesmoAlterandoUuid() throws Exception {
        exemplo(); String id=gerarId();
        mvc.perform(get("/api/resumos/"+id).with(user(B)).header("X-Diario-Usuario",B)).andExpect(status().isNotFound());
        mvc.perform(get("/api/resumos").with(user(B)).header("X-Diario-Usuario",B)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/resumos/"+id).with(user(B)).header("X-Diario-Usuario",A)).andExpect(status().isConflict());
    }
    @Test void rejeitaSemConfirmacaoOuCsrf() throws Exception {
        exemplo(); gerar(A,DIA.toString(),DIA.toString(),false).andExpect(status().isBadRequest());
        mvc.perform(post("/api/resumos").with(user(A)).header("X-Diario-Usuario",A)).andExpect(status().isForbidden());
        mvc.perform(get("/api/resumos")).andExpect(status().isUnauthorized());
        verify(sumarizador,never()).resumir(anyList());
    }
    @Test void periodoVazioOuMuitoCurtoNaoChamaAPI() throws Exception {
        gerar(A,DIA.toString(),DIA.toString(),true).andExpect(status().isUnprocessableEntity());
        escrever(A,DIA,"Oi.");
        gerar(A,DIA.toString(),DIA.toString(),true).andExpect(status().isUnprocessableEntity());
        verify(sumarizador,never()).resumir(anyList());
    }
    @Test void rejeitaPeriodoInvalidoELimiteSemCortar() throws Exception {
        gerar(A,"2026-10-27","2026-10-26",true).andExpect(status().isBadRequest());
        gerar(A,"2026-09-01","2026-10-27",true).andExpect(status().isBadRequest());
        escrever(A,DIA,"a".repeat(13000)); escrever(A,DIA.minusDays(1),"b".repeat(13000));
        gerar(A,"2026-10-26","2026-10-27",true).andExpect(status().isPayloadTooLarge());
        verify(sumarizador,never()).resumir(anyList());
    }
    @Test void editarRemoverOuInserirNoPeriodoInvalidaResumo() throws Exception {
        exemplo(); gerarId();
        registros.salvar(A,DIA,"Relato fictício atualizado: fiquei em dúvida sobre o resultado.",0);
        assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isZero();
        gerarId(); registros.excluir(A,DIA,1);
        assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isZero();
        escrever(A,DIA,"Relato fictício novo para gerar outro resumo do período.");
        gerar(A,"2026-10-26","2026-10-27",true).andExpect(status().isOk());
        escrever(A,DIA.minusDays(1),"Página criada dentro do período após a geração do resumo.");
        assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isZero();
    }
    @Test void alteracaoForaDoPeriodoOuEmOutraContaNaoInvalida() throws Exception {
        exemplo(); gerarId();
        escrever(A,DIA.minusDays(3),"Relato fictício fora do período, sem invalidar o resumo anterior.");
        escrever(B,DIA,"Página da outra conta, sem afetar os resumos da primeira.");
        assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isEqualTo(1);
    }
    @Test void falhaDoProvedorNaoGravaResumoENovaTentativaPodeFuncionar() throws Exception {
        exemplo(); when(sumarizador.resumir(anyList())).thenThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Cota atingida."))
            .thenReturn(new Sumarizador.Resultado("Resumo fictício da tentativa seguinte.","modelo-teste"));
        gerar(A,DIA.toString(),DIA.toString(),true).andExpect(status().isTooManyRequests());
        assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isZero();
        gerar(A,DIA.toString(),DIA.toString(),true).andExpect(status().isOk());
    }

    @Test @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void bloqueiaSolicitacaoDuplicadaENaoSalvaSeRegistroMudarDuranteAPI() throws Exception {
        var entrou=new CountDownLatch(1);
        var liberar=new CountDownLatch(1);
        var executor=Executors.newSingleThreadExecutor();
        try {
            exemplo();
            when(sumarizador.resumir(anyList())).thenAnswer(invocacao->{
                entrou.countDown();
                if (!liberar.await(15,TimeUnit.SECONDS)) throw new IllegalStateException("Tempo do teste esgotado.");
                return new Sumarizador.Resultado("Resumo fictício da versão anterior.","modelo-teste");
            });
            var primeira=executor.submit(()->servicoResumos.gerar(A,DIA,DIA));
            assertThat(entrou.await(10,TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(()->servicoResumos.gerar(A,DIA,DIA)).isInstanceOfSatisfying(ResponseStatusException.class,
                e->assertThat(e.getStatusCode().value()).isEqualTo(409));
            registros.salvar(A,DIA,"Relato fictício modificado durante a chamada da API externa.",0);
            liberar.countDown();
            assertThatThrownBy(()->primeira.get(10,TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ResponseStatusException.class);
            assertThat(banco.queryForObject("select count(*) from resumos",Long.class)).isZero();
            verify(sumarizador,times(1)).resumir(anyList());
        } finally {
            liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(5,TimeUnit.SECONDS);
            banco.update("delete from resumos where usuario_id in (select id from usuarios where login in (?,?))",A,B);
            banco.update("delete from registros where usuario_id in (select id from usuarios where login in (?,?))",A,B);
            banco.update("delete from usuarios where login in (?,?)",A,B);
        }
    }
}
