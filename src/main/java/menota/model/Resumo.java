package menota.model;

import java.time.LocalDate;

public record Resumo(
        LocalDate inicio,
        LocalDate fim,
        String texto
) {
}