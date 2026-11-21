package menota.controller;

import menota.service.NotaService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class NotaController {

    private final NotaService service;

    public NotaController(NotaService service) {
        this.service = service;
    }

    @GetMapping("/paciente")
    public String abrirAgenda(
            Authentication autenticacao,
            Model model
    ) {
        model.addAttribute("notas", service.listar(autenticacao.getName()));
        model.addAttribute("email", autenticacao.getName());
        model.addAttribute("hoje", LocalDate.now());

        if (!model.containsAttribute("texto")) {
            model.addAttribute("texto", "");
        }

        return "agenda";
    }

    @PostMapping("/paciente/notas")
    public String registrar(
            Authentication autenticacao,
            @RequestParam("texto") String texto,
            RedirectAttributes atributos
    ) {
        try {
            service.registrar(autenticacao.getName(), texto);

            atributos.addFlashAttribute(
                    "sucesso",
                    "Sua anotação foi registrada."
            );

        } catch (IllegalArgumentException erro) {
            atributos.addFlashAttribute("erro", erro.getMessage());
            atributos.addFlashAttribute("texto", texto);

        } catch (DataAccessException erro) {
            atributos.addFlashAttribute(
                    "erro",
                    "Não foi possível registrar. Tente novamente."
            );
            atributos.addFlashAttribute("texto", texto);
            erro.printStackTrace();
        }

        return "redirect:/paciente";
    }
}