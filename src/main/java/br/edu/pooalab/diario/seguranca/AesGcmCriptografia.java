package br.edu.pooalab.diario.seguranca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

// Adapter: oferece uma interface pequena para a API de criptografia do Java.
@Component
public class AesGcmCriptografia implements CriptografiaTexto {
    private final SecretKeySpec chave;
    private final SecureRandom aleatorio = new SecureRandom();

    public AesGcmCriptografia(@Value("${diario.key}") String chaveBase64) {
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(chaveBase64); }
        catch (IllegalArgumentException e) { throw new IllegalStateException("DIARIO_KEY deve ser uma chave Base64 válida."); }
        if (bytes.length != 32) throw new IllegalStateException("DIARIO_KEY deve conter exatamente 32 bytes.");
        this.chave = new SecretKeySpec(bytes, "AES");
    }

    @Override
    public String cifrar(String texto) {
        byte[] iv = new byte[12];
        aleatorio.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(128, iv));
            byte[] resultado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + resultado.length)
                    .put(iv).put(resultado).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger o registro.");
        }
    }

    @Override
    public String decifrar(String textoCifrado) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(textoCifrado));
            if (buffer.remaining() < 28) throw new IllegalArgumentException();
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] dados = new byte[buffer.remaining()];
            buffer.get(dados);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(dados), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível abrir o registro. Verifique a chave de criptografia.");
        }
    }
}
