package menota.controller;

import menota.service.ResumoService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResumoController {

    private final ResumoService service;

    public ResumoController(ResumoService service) {
        this.service = service;
    }

    @GetMapping("/paciente/resumo")
    public String abrir(Model model) {
        if (!model.containsAttribute("dias")) {
            model.addAttribute("dias", 7);
        }

        return "resumo";
    }

    @PostMapping("/paciente/resumo")
    public String gerar(
            Authentication autenticacao,
            @RequestParam("dias") int dias,
            RedirectAttributes atributos
    ) {
        atributos.addFlashAttribute("dias", dias);

        try {
            atributos.addFlashAttribute(
                    "resumo",
                    service.gerar(autenticacao.getName(), dias)
            );

        } catch (IllegalArgumentException | IllegalStateException erro) {
            atributos.addFlashAttribute("erro", erro.getMessage());

        } catch (DataAccessException erro) {
            atributos.addFlashAttribute(
                    "erro",
                    "Não foi possível consultar suas anotações."
            );
        }

        return "redirect:/paciente/resumo";
    }
}