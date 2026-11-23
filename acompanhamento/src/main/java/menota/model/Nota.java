package menota.model;

import java.time.LocalDate;

public record Nota(
        Long id,
        LocalDate data,
        String texto
) {
}