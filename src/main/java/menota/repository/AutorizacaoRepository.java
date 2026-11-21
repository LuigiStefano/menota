package menota.repository;

import menota.model.Profissional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AutorizacaoRepository {

    private final JdbcTemplate jdbc;

    public AutorizacaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Profissional> listarAutorizados(Long pacienteId) {
        String sql = """
                SELECT u.id, u.nome, u.email
                FROM autorizacoes a
                JOIN usuarios u
                    ON u.id = a.profissional_id
                WHERE a.paciente_id = ?
                  AND u.perfil = 'PROFISSIONAL'
                ORDER BY u.nome, u.id
                """;

        return jdbc.query(
                sql,
                (resultado, linha) -> new Profissional(
                        resultado.getLong("id"),
                        resultado.getString("nome"),
                        resultado.getString("email")
                ),
                pacienteId
        );
    }

    public void autorizar(Long pacienteId, Long profissionalId) {
        String sql = """
                INSERT INTO autorizacoes (paciente_id, profissional_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        jdbc.update(sql, pacienteId, profissionalId);
    }

    public void revogar(Long pacienteId, Long profissionalId) {
        String sql = """
                DELETE FROM autorizacoes
                WHERE paciente_id = ?
                  AND profissional_id = ?
                """;

        jdbc.update(sql, pacienteId, profissionalId);
    }
}