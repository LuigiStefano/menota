package br.edu.pooalab.diario.usuario;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String login;
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false, length = 20)
    private String perfil = "PACIENTE";

    protected Usuario() {}

    public Usuario(String login, String senhaHash) {
        this.login = login;
        this.senhaHash = senhaHash;
    }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public boolean isProfissional() { return "PROFISSIONAL".equals(perfil); }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
}
