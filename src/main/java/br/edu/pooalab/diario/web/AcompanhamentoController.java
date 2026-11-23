package br.edu.pooalab.diario.web;

import br.edu.pooalab.diario.acompanhamento.AcompanhamentoService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/acompanhamento")
public class AcompanhamentoController {
    private final AcompanhamentoService servico;
    public AcompanhamentoController(AcompanhamentoService servico) { this.servico = servico; }

    @ModelAttribute
    void conferirConta(Principal conta, HttpServletRequest pedido, HttpServletResponse resposta) {
        resposta.setHeader("Cache-Control", "no-store");
        if (!conta.getName().equals(pedido.getHeader("X-Diario-Usuario")))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A conta desta aba mudou. Entre novamente.");
    }
    public record Autorizacao(@NotBlank @Pattern(regexp="[a-z0-9_]{3,30}") String profissional,
                              @AssertTrue boolean autorizo) {}
    @GetMapping("/acessos")
    List<AcompanhamentoService.Conta> acessos(Principal conta) { return servico.acessos(conta.getName()); }
    @PostMapping("/acessos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void autorizar(Principal conta, @Valid @RequestBody Autorizacao pedido) {
        servico.autorizar(conta.getName(), pedido.profissional());
    }
    @DeleteMapping("/acessos/{profissional}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revogar(Principal conta, @PathVariable String profissional) { servico.revogar(conta.getName(), profissional); }
    @GetMapping("/pacientes")
    List<AcompanhamentoService.Conta> pacientes(Principal conta) { return servico.pacientes(conta.getName()); }
    @GetMapping("/pacientes/{paciente}")
    AcompanhamentoService.Periodo consultar(Principal conta, @PathVariable String paciente,
            @RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return servico.consultar(conta.getName(), paciente, inicio, fim);
    }
}
