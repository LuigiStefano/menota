package br.edu.pooalab.diario.resumo;

import java.time.LocalDate;
import br.edu.pooalab.diario.registro.BemEstar;
import java.util.List;

public interface Sumarizador {
    record Configuracao(String modelo, String versao) {}
    record Entrada(LocalDate data, String texto, BemEstar bemEstar) {
        public Entrada(LocalDate data, String texto) { this(data, texto, BemEstar.vazio()); }
    }
    record Emocao(LocalDate data, String nome, String trecho, boolean incerta) {}
    record Resultado(String texto, String modelo, List<Emocao> emocoes) {
        public Resultado(String texto, String modelo) { this(texto, modelo, List.of()); }
    }
    Resultado resumir(List<Entrada> registros);
    boolean configurado();
    Configuracao configuracao();
}
