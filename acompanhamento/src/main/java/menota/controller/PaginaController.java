package menota.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String inicio(Authentication autenticacao) {
        for (GrantedAuthority permissao : autenticacao.getAuthorities()) {
            if (permissao.getAuthority().equals("ROLE_PROFISSIONAL")) {
                return "redirect:/profissional";
            }
        }

        return "redirect:/paciente";
    }
}