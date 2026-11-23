package br.edu.pooalab.diario.web;

import br.edu.pooalab.diario.resumo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/resumos")
public class ResumoController {
    private final ResumoService resumos;
    public ResumoController(ResumoService resumos) { this.resumos = resumos; }

    @ModelAttribute
    void conferirConta(Principal usuario, HttpServletRequest requisicao) {
        if (!usuario.getName().equals(requisicao.getHeader("X-Diario-Usuario")))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A conta desta aba mudou. Entre novamente com a conta original.");
    }

    public record Pedido(@NotNull LocalDate inicio, @NotNull LocalDate fim,
                         @AssertTrue boolean confirmoEnvioFicticio) {}

    @GetMapping("/configuracao")
    Map<String, Object> configuracao() {
        return Map.of("configurado", resumos.configurado(), "modelo", resumos.modelo(),
            "maximoDias", 31, "maximoCaracteres", 24000);
    }

    @GetMapping
    List<ResumoService.Item> listar(Principal usuario) { return resumos.listar(usuario.getName()); }

    @GetMapping("/{id}")
    ResumoService.Detalhe consultar(Principal usuario, @PathVariable UUID id) { return resumos.consultar(usuario.getName(), id); }

    @PostMapping
    ResumoService.Geracao gerar(Principal usuario, @Valid @RequestBody Pedido pedido) {
        return resumos.gerar(usuario.getName(), pedido.inicio(), pedido.fim());
    }
}
