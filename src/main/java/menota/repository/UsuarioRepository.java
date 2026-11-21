package menota.repository;

import menota.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = """
            SELECT id, nome, email, senha, perfil
            FROM usuarios
            WHERE email = ?
            """;

        List<Usuario> usuarios = jdbc.query(
                sql,
                (resultado, linha) -> new Usuario(
                        resultado.getLong("id"),
                        resultado.getString("nome"),
                        resultado.getString("email"),
                        resultado.getString("senha"),
                        resultado.getString("perfil")
                ),
                email
        );

        if (usuarios.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(usuarios.get(0));
    }



    public void inserirSeAusente(
            String nome,
            String email,
            String senha,
            String perfil
    ) {
        String sql = """
                INSERT INTO usuarios (nome, email, senha, perfil)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (email) DO NOTHING
                """;

        jdbc.update(sql, nome, email, senha, perfil);
    }
}