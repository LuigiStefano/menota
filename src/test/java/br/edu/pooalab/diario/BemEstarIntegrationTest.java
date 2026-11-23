package br.edu.pooalab.diario;

import br.edu.pooalab.diario.usuario.UsuarioService;
import br.edu.pooalab.diario.registro.*;
import br.edu.pooalab.diario.resumo.*;
import br.edu.pooalab.diario.acompanhamento.AcompanhamentoService;
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
class BemEstarIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarios;
    @Autowired RegistroService registros;
    @Autowired ResumoService resumos;
    @Autowired AcompanhamentoService acompanhamento;
    @Autowired JdbcTemplate banco;
    @Autowired CriptografiaTexto criptografia;
    @Autowired ObjectMapper json;
    @MockitoBean Sumarizador sumarizador;
    static final String A="humor_a", B="humor_b";
    static final LocalDate DIA=LocalDate.of(2026,10,29);
    static final String TEXTO="Relato fictício: terminei uma atividade e fiquei contente, mas estava cansado.";
    @BeforeEach void preparar() {
        when(sumarizador.configuracao()).thenReturn(new Sumarizador.Configuracao("modelo-teste", "configuracao-teste"));
        usuarios.cadastrar(A,"SenhaFicticia123!");usuarios.cadastrar(B,"SenhaFicticia123!");
        when(sumarizador.resumir(anyList())).thenReturn(new Sumarizador.Resultado("Resumo fictício do período.","modelo-teste"));
    }
    ResultActions salvar(Object bemEstar,long versao) throws Exception {
        Map<String,Object> dados=new HashMap<>(Map.of("data",DIA.toString(),"texto",TEXTO,"versao",versao));
        if(bemEstar!=null)dados.put("bemEstar",bemEstar);
        return mvc.perform(put("/api/registros").with(user(A).roles("PACIENTE")).with(csrf()).header("X-Diario-Usuario",A)
            .contentType("application/json").content(json.writeValueAsString(dados)));
    }
    ResultActions datas(String login) throws Exception {
        return mvc.perform(get("/api/registros/datas").with(user(login).roles("PACIENTE")).header("X-Diario-Usuario",login).param("mes","2026-10"));
    }
    @Test void salvaHumorCifradoEReabreSemCompartilharComOutraConta() throws Exception {
        salvar(Map.of("humor","BOM","energia","BAIXA"),-1).andExpect(status().isOk())
            .andExpect(jsonPath("$.bemEstar.humor").value("BOM")).andExpect(jsonPath("$.bemEstar.energia").value("BAIXA"));
        String cifrado=banco.queryForObject("select bem_estar_cifrado from registros where usuario_id=(select id from usuarios where login=?)",String.class,A);
        assertThat(cifrado).doesNotContain("BOM","BAIXA");
        assertThat(criptografia.decifrar(cifrado)).contains("BOM","BAIXA");
        assertThat(registros.buscar(A,DIA).bemEstar().humor()).isEqualTo(BemEstar.Humor.BOM);
        assertThat(registros.buscar(B,DIA).bemEstar()).isEqualTo(BemEstar.vazio());
    }
    @Test void clientesAntigosPreservamCamposEAInterfacePodeLimpar() throws Exception {
        salvar(Map.of("humor","BAIXO"),-1).andExpect(status().isOk());
        salvar(null,0).andExpect(status().isOk()).andExpect(jsonPath("$.bemEstar.humor").value("BAIXO"));
        long versao=registros.buscar(A,DIA).versao();
        salvar(Map.of(),versao).andExpect(status().isOk());
        assertThat(registros.buscar(A,DIA).bemEstar()).isEqualTo(BemEstar.vazio());
    }
    @Test void rejeitaCategoriaInvalidaENumeroSemSalvarNota() throws Exception {
        salvar(Map.of("humor","DIAGNOSTICO"),-1).andExpect(status().isBadRequest());
        salvar(Map.of("energia",8),-1).andExpect(status().isBadRequest());
        assertThat(registros.buscar(A,DIA).versao()).isEqualTo(-1);
    }
    @Test void calendarioRetornaSoDatasDoDonoEDoMesERefleteExclusao() throws Exception {
        registros.salvar(A,DIA,TEXTO,-1);
        registros.salvar(A,DIA.plusMonths(1),TEXTO,-1);
        registros.salvar(B,DIA.minusDays(1),TEXTO,-1);
        var resultado=datas(A).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0]").value(DIA.toString())).andReturn();
        assertThat(resultado.getResponse().getContentAsString()).doesNotContain(TEXTO,"humor","energia",B);
        mvc.perform(get("/api/registros/datas").param("mes","2026-10")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/registros/datas").with(user(A).roles("PACIENTE")).header("X-Diario-Usuario",B).param("mes","2026-10")).andExpect(status().isConflict());
        mvc.perform(get("/api/registros/datas").with(user(A).roles("PACIENTE")).header("X-Diario-Usuario",A).param("mes","2026-13")).andExpect(status().isBadRequest());
        registros.excluir(A,DIA,0);datas(A).andExpect(jsonPath("$.length()").value(0));
    }
    @Test void mudarSomenteHumorInvalidaResumoEInformaContextoSemPerderVersao() throws Exception {
        salvar(Map.of("humor","BOM","energia","BAIXA"),-1).andExpect(status().isOk());
        resumos.gerar(A,DIA,DIA);
        verify(sumarizador).resumir(argThat(lista->lista.get(0).bemEstar().humor()==BemEstar.Humor.BOM
            && lista.get(0).bemEstar().energia()==BemEstar.Energia.BAIXA));
        salvar(Map.of("humor","BAIXO"),0).andExpect(status().isOk());
        assertThat(resumos.listar(A)).isEmpty();
        salvar(Map.of("humor","MUITO_BOM"),0).andExpect(status().isConflict());
        assertThat(registros.buscar(A,DIA).bemEstar().humor()).isEqualTo(BemEstar.Humor.BAIXO);
    }
    @Test void profissionalVeAutorrelatoSomenteComAutorizacao() throws Exception {
        salvar(Map.of("humor","NEUTRO","energia","MEDIA"),-1).andExpect(status().isOk());
        usuarios.cadastrar("humor_prof","SenhaFicticia123!","PROFISSIONAL");
        assertThatThrownBy(()->acompanhamento.consultar("humor_prof",A,DIA,DIA))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        acompanhamento.autorizar(A,"humor_prof");
        assertThat(acompanhamento.consultar("humor_prof",A,DIA,DIA).registros().get(0).bemEstar())
            .isEqualTo(new BemEstar(BemEstar.Humor.NEUTRO,BemEstar.Energia.MEDIA));
    }
}
