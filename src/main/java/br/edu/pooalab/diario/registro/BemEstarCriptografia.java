package br.edu.pooalab.diario.registro;

import br.edu.pooalab.diario.seguranca.CriptografiaTexto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;

// Centraliza a leitura cifrada usada pelo diário, resumo e acompanhamento.
@Component
public class BemEstarCriptografia {
    private final CriptografiaTexto criptografia;
    private final ObjectMapper json;
    public BemEstarCriptografia(CriptografiaTexto criptografia, ObjectMapper json) {
        this.criptografia = criptografia; this.json = json;
    }
    public String cifrar(BemEstar valor) {
        try { return criptografia.cifrar(json.writeValueAsString(valor)); }
        catch (IOException e) { throw new IllegalStateException("Não foi possível salvar o humor."); }
    }
    public BemEstar ler(String cifrado) {
        if (cifrado == null) return BemEstar.vazio();
        try { return json.readValue(criptografia.decifrar(cifrado), BemEstar.class); }
        catch (IOException e) { throw new IllegalStateException("Não foi possível ler o humor."); }
    }
}
