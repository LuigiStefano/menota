package br.edu.pooalab.diario.registro;

import br.edu.pooalab.diario.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registros", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "data"}))
public class Registro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false)
    private LocalDate data;
    @Column(name = "texto_cifrado", nullable = false, columnDefinition = "text")
    private String textoCifrado;
    @Column(name = "bem_estar_cifrado", columnDefinition = "text")
    private String bemEstarCifrado;
    @Version
    private Long versao;

    protected Registro() {}
    public Registro(Usuario usuario, LocalDate data) { this.usuario = usuario; this.data = data; }
    public LocalDate getData() { return data; }
    public Long getId() { return id; }
    public String getTextoCifrado() { return textoCifrado; }
    public void setTextoCifrado(String textoCifrado) { this.textoCifrado = textoCifrado; }
    public Long getVersao() { return versao; }
    public String getBemEstarCifrado() { return bemEstarCifrado; }
    public void setBemEstarCifrado(String valor) { this.bemEstarCifrado = valor; }
}
