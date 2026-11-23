package br.edu.pooalab.diario.resumo;

import br.edu.pooalab.diario.usuario.Usuario;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "resumos")
public class Resumo {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false) private LocalDate inicio;
    @Column(nullable = false) private LocalDate fim;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "texto_cifrado", nullable = false, columnDefinition = "text") private String textoCifrado;
    @Column(name = "fontes_json", nullable = false, columnDefinition = "text") private String fontesJson;
    @Column(nullable = false, length = 100) private String modelo;
    @Column(nullable = false, length = 100) private String configuracao;
    @Column(nullable = false, length = 64) private String assinatura;

    @Column(name = "emocoes_cifradas", columnDefinition = "text") private String emocoesCifradas;
    public String getEmocoesCifradas() { return emocoesCifradas; }
    public void setEmocoesCifradas(String valor) { this.emocoesCifradas = valor; }

    protected Resumo() {}
    public Resumo(Usuario usuario, LocalDate inicio, LocalDate fim, String textoCifrado,
                  String fontesJson, String modelo, String configuracao, String assinatura) {
        this.usuario = usuario; this.inicio = inicio; this.fim = fim; this.criadoEm = Instant.now();
        this.textoCifrado = textoCifrado; this.fontesJson = fontesJson; this.modelo = modelo;
        this.configuracao = configuracao; this.assinatura = assinatura;
    }
    public UUID getId() { return id; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getTextoCifrado() { return textoCifrado; }
    public String getFontesJson() { return fontesJson; }
    public String getModelo() { return modelo; }
    public String getConfiguracao() { return configuracao; }
}
