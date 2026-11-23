package br.edu.pooalab.diario.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder senhas) {
        this.usuarios = usuarios;
        this.senhas = senhas;
    }

    @Transactional
    public void cadastrar(String login, String senha) { cadastrar(login, senha, "PACIENTE"); }

    @Transactional
    public void cadastrar(String login, String senha, String perfil) {
        if (!java.util.Set.of("PACIENTE", "PROFISSIONAL").contains(perfil)) throw new IllegalArgumentException("Escolha um perfil válido.");
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha é longa demais. Use uma senha mais curta.");
        }
        var usuario = new Usuario(login, senhas.encode(senha));
        usuario.setPerfil(perfil);
        usuarios.saveAndFlush(usuario);
    }
}
