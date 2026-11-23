package br.edu.pooalab.diario.seguranca;

public interface CriptografiaTexto {
    String cifrar(String texto);
    String decifrar(String textoCifrado);
}
