package menota.controller;

import menota.service.AutorizacaoService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutorizacaoController {

    private final AutorizacaoService service;

    public AutorizacaoController(AutorizacaoService service) {
        this.service = service;
    }

    @GetMapping("/paciente/autorizacoes")
    public String abrir(
            Authentication autenticacao,
            Model model
    ) {
        model.addAttribute(
                "profissionais",
                service.listarAutorizados(autenticacao.getName())
        );

        if (!model.containsAttribute("emailProfissional")) {
            model.addAttribute("emailProfissional", "");
        }

        return "autorizacoes";
    }

    @PostMapping("/paciente/autorizacoes")
    public String autorizar(
            Authentication autenticacao,
            @RequestParam(
                    name = "emailProfissional",
                    defaultValue = ""
            ) String emailProfissional,
            RedirectAttributes atributos
    ) {
        try {
            service.autorizar(
                    autenticacao.getName(),
                    emailProfissional
            );

            atributos.addFlashAttribute(
                    "sucesso",
                    "Acesso autorizado."
            );

        } catch (IllegalArgumentException erro) {
            atributos.addFlashAttribute("erro", erro.getMessage());
            atributos.addFlashAttribute(
                    "emailProfissional",
                    emailProfissional
            );

        } catch (DataAccessException erro) {
            atributos.addFlashAttribute(
                    "erro",
                    "Não foi possível autorizar o acesso."
            );
            atributos.addFlashAttribute(
                    "emailProfissional",
                    emailProfissional
            );
            erro.printStackTrace();
        }

        return "redirect:/paciente/autorizacoes";
    }

    @PostMapping("/paciente/autorizacoes/{profissionalId}/revogar")
    public String revogar(
            Authentication autenticacao,
            @PathVariable("profissionalId") Long profissionalId,
            RedirectAttributes atributos
    ) {
        try {
            service.revogar(
                    autenticacao.getName(),
                    profissionalId
            );

            atributos.addFlashAttribute(
                    "sucesso",
                    "Acesso revogado."
            );

        } catch (DataAccessException erro) {
            atributos.addFlashAttribute(
                    "erro",
                    "Não foi possível revogar o acesso."
            );
            erro.printStackTrace();
        }

        return "redirect:/paciente/autorizacoes";
    }
}