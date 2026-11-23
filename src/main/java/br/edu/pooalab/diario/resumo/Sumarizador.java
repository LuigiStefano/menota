package br.edu.pooalab.diario.resumo;

import java.time.LocalDate;
import java.util.List;

public interface Sumarizador {
    record Entrada(LocalDate data, String texto) {}
    record Resultado(String texto, String modelo) {}
    Resultado resumir(List<Entrada> registros);
    boolean configurado();
}
