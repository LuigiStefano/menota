package menota.repository;

import menota.model.Nota;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class NotaRepository {

    private final JdbcTemplate jdbc;

    public NotaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void salvar(
            Long pacienteId,
            LocalDate data,
            String texto
    ) {
        String sql = """
                INSERT INTO notas (paciente_id, data, texto)
                VALUES (?, ?, ?)
                """;

        jdbc.update(sql, pacienteId, Date.valueOf(data), texto);
    }

    public List<Nota> listarPorPaciente(Long pacienteId) {
        String sql = """
                SELECT id, data, texto
                FROM notas
                WHERE paciente_id = ?
                ORDER BY data DESC, id DESC
                """;

        return jdbc.query(
                sql,
                (resultado, linha) -> new Nota(
                        resultado.getLong("id"),
                        resultado.getDate("data").toLocalDate(),
                        resultado.getString("texto")
                ),
                pacienteId
        );
    }
    public List<Nota> listarPorPeriodo(
            Long pacienteId,
            LocalDate inicio,
            LocalDate fim
    ) {
        String sql = """
            SELECT id, data, texto
            FROM notas
            WHERE paciente_id = ?
              AND data BETWEEN ? AND ?
            ORDER BY data DESC, id DESC
            """;

        return jdbc.query(
                sql,
                (resultado, linha) -> new Nota(
                        resultado.getLong("id"),
                        resultado.getDate("data").toLocalDate(),
                        resultado.getString("texto")
                ),
                pacienteId,
                Date.valueOf(inicio),
                Date.valueOf(fim)
        );
    }
}