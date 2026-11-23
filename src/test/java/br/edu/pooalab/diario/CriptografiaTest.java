package br.edu.pooalab.diario;

import br.edu.pooalab.diario.seguranca.AesGcmCriptografia;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class CriptografiaTest {
    private final AesGcmCriptografia crypto = new AesGcmCriptografia("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");

    @Test void rejeitaChaveInvalidaSemExporSeuValor() {
        assertThatThrownBy(() -> new AesGcmCriptografia("segredo-invalido")).isInstanceOf(IllegalStateException.class)
            .hasMessageNotContaining("segredo-invalido");
    }
    @Test void rejeitaTextoAdulterado() {
        byte[] bytes = Base64.getDecoder().decode(crypto.cifrar("Relato fictício."));
        bytes[bytes.length - 1] ^= 1;
        assertThatThrownBy(() -> crypto.decifrar(Base64.getEncoder().encodeToString(bytes)))
            .isInstanceOf(IllegalStateException.class);
    }
    @Test void chaveErradaNaoAbreRegistro() {
        var outra = new AesGcmCriptografia("AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE=");
        assertThatThrownBy(() -> outra.decifrar(crypto.cifrar("Texto fictício"))).isInstanceOf(IllegalStateException.class);
    }
}
