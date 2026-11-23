package br.edu.pooalab.diario.web;

import br.edu.pooalab.diario.registro.RegistroService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class RegistroController {
    private final RegistroService registros;
    public RegistroController(RegistroService registros) { this.registros = registros; }

    @ModelAttribute
    void conferirConta(Principal usuario, HttpServletRequest requisicao) {
        if (!requisicao.getRequestURI().equals("/api/sessao") &&
                !usuario.getName().equals(requisicao.getHeader("X-Diario-Usuario"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "A conta desta aba precisa ser confirmada. Entre novamente com o usuário original.");
        }
    }

    public record Escrita(@NotNull LocalDate data,
                          @NotBlank @Size(max = 20000) String texto,
                          @NotNull @Min(-1) Long versao) {}

    @GetMapping("/registros")
    RegistroService.Pagina buscar(Principal usuario,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return registros.buscar(usuario.getName(), data);
    }

    @PutMapping("/registros")
    RegistroService.Pagina salvar(Principal usuario, @Valid @RequestBody Escrita escrita) {
        return registros.salvar(usuario.getName(), escrita.data(), escrita.texto(), escrita.versao());
    }

    @DeleteMapping("/registros")
    void excluir(Principal usuario,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @RequestParam long versao, HttpServletResponse resposta) {
        registros.excluir(usuario.getName(), data, versao);
        resposta.setStatus(204);
    }

    @GetMapping("/sessao")
    Map<String, String> sessao(Principal usuario, CsrfToken csrf) {
        return Map.of("login", usuario.getName(), "token", csrf.getToken(), "cabecalho", csrf.getHeaderName());
    }
}
