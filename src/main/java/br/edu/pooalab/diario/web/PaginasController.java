package br.edu.pooalab.diario.web;

import br.edu.pooalab.diario.usuario.UsuarioService;
import br.edu.pooalab.diario.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
public class PaginasController {
    private final UsuarioService usuarios;
    private final UsuarioRepository contas;
    public PaginasController(UsuarioService usuarios, UsuarioRepository contas) { this.usuarios = usuarios; this.contas = contas; }

    public static class Cadastro {
        @Pattern(regexp = "[a-z0-9_]{3,30}", message = "Use de 3 a 30 caracteres: letras minúsculas, números ou _.")
        private String login = "";
        @Size(min = 12, max = 72, message = "A senha deve ter de 12 a 72 caracteres.")
        private String senha = "";
        @Pattern(regexp = "PACIENTE|PROFISSIONAL", message = "Escolha seu perfil.")
        private String perfil = "PACIENTE";
        public String getPerfil() { return perfil; }
        public void setPerfil(String perfil) { this.perfil = perfil; }
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/cadastro")
    String cadastro(Model model) { model.addAttribute("cadastro", new Cadastro()); return "cadastro"; }

    @PostMapping("/cadastro")
    String cadastrar(@Valid @ModelAttribute("cadastro") Cadastro cadastro, BindingResult erros) {
        if (!erros.hasErrors()) {
            try {
                usuarios.cadastrar(cadastro.getLogin(), cadastro.getSenha(), cadastro.getPerfil());
                return "redirect:/login?cadastro";
            } catch (DataIntegrityViolationException e) {
                erros.rejectValue("login", "duplicado", "Escolha outro nome de usuário.");
            } catch (IllegalArgumentException e) {
                erros.rejectValue("senha", "tamanho", e.getMessage());
            }
        }
        cadastro.setSenha("");
        return "cadastro";
    }

    @GetMapping("/")
    String diario(Model model, Principal principal) {
        if (contas.findByLogin(principal.getName()).orElseThrow().isProfissional()) return "redirect:/profissional";
        model.addAttribute("login", principal.getName());
        return "diario";
    }

    @GetMapping("/compartilhamento")
    String compartilhamento(Model model, Principal principal) {
        if (contas.findByLogin(principal.getName()).orElseThrow().isProfissional()) return "redirect:/profissional";
        model.addAttribute("login", principal.getName());
        return "compartilhamento";
    }

    @GetMapping("/profissional")
    String profissional(Model model, Principal principal) {
        if (!contas.findByLogin(principal.getName()).orElseThrow().isProfissional()) return "redirect:/";
        model.addAttribute("login", principal.getName());
        return "profissional";
    }

    @GetMapping("/resumos")
    String resumos(Model model, Principal principal) {
        if (contas.findByLogin(principal.getName()).orElseThrow().isProfissional()) return "redirect:/profissional";
        model.addAttribute("login", principal.getName());
        return "resumos";
    }
}
