package menota.model;

public record Usuario(
        Long id,
        String nome,
        String email,
        String senha,
        String perfil
) {
}