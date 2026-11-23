package br.edu.pooalab.diario.web;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {RegistroController.class, ResumoController.class})
public class ErrosApi {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> conflito(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(Map.of("erro", erro.getReason()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
                      MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, String>> invalido() {
        return ResponseEntity.badRequest().body(Map.of("erro", "Confira os campos, as datas e a confirmação solicitada. Registros aceitam de 1 a 20.000 caracteres."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> indisponivel() {
        // Não registra a exceção: SQL, parâmetros e textos pessoais não devem ir para os logs.
        return ResponseEntity.status(503).body(Map.of("erro", "Não foi possível concluir. Seu texto continua na tela; tente novamente."));
    }
}
