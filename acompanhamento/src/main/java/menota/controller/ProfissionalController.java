package menota.controller;

import menota.service.ProfissionalService;
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
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @GetMapping("/profissional")
    public String abrir(
            Authentication autenticacao,
            Model model
    ) {
        model.addAttribute(
                "pacientes",
                service.listarPacientes(autenticacao.getName())
        );

        return "profissional";
    }

    @GetMapping("/profissional/pacientes/{pacienteId}")
    public String consultar(
            Authentication autenticacao,
            @PathVariable("pacienteId") Long pacienteId,
            Model model
    ) {
        model.addAttribute(
                "paciente",
                service.buscarPacienteAutorizado(
                        autenticacao.getName(),
                        pacienteId
                )
        );

        model.addAttribute(
                "notas",
                service.listarNotas(
                        autenticacao.getName(),
                        pacienteId
                )
        );

        if (!model.containsAttribute("dias")) {
            model.addAttribute("dias", 7);
        }

        return "acompanhamento";
    }

    @PostMapping("/profissional/pacientes/{pacienteId}/resumo")
    public String gerarResumo(
            Authentication autenticacao,
            @PathVariable("pacienteId") Long pacienteId,
            @RequestParam("dias") int dias,
            RedirectAttributes atributos
    ) {
        atributos.addFlashAttribute("dias", dias);

        try {
            atributos.addFlashAttribute(
                    "resumo",
                    service.gerarResumo(
                            autenticacao.getName(),
                            pacienteId,
                            dias
                    )
            );

        } catch (IllegalArgumentException | IllegalStateException erro) {
            atributos.addFlashAttribute("erro", erro.getMessage());

        } catch (DataAccessException erro) {
            atributos.addFlashAttribute(
                    "erro",
                    "Não foi possível consultar os registros."
            );
        }

        return "redirect:/profissional/pacientes/" + pacienteId;
    }
}