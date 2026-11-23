package br.edu.pooalab.diario.acompanhamento;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
class AcessoRepository {
    record Acesso(String login, Instant desde) {}

    private final JdbcTemplate banco;

    AcessoRepository(JdbcTemplate banco) {
        this.banco = banco;
    }

    List<Acesso> profissionaisDo(long pacienteId) {
        return banco.query("""
            select u.login, a.criado_em
            from acessos_acompanhamento a join usuarios u on u.id = a.profissional_id
            where a.paciente_id = ? order by u.login
            """, (linha, indice) -> new Acesso(linha.getString(1), linha.getTimestamp(2).toInstant()), pacienteId);
    }

    List<Acesso> pacientesDo(long profissionalId) {
        return banco.query("""
            select u.login, a.criado_em
            from acessos_acompanhamento a join usuarios u on u.id = a.paciente_id
            where a.profissional_id = ? order by u.login
            """, (linha, indice) -> new Acesso(linha.getString(1), linha.getTimestamp(2).toInstant()), profissionalId);
    }

    void autorizar(long pacienteId, long profissionalId, Instant criadaEm) {
        banco.update("""
            insert into acessos_acompanhamento (paciente_id, profissional_id, criado_em)
            values (?, ?, ?) on conflict (paciente_id, profissional_id) do nothing
            """, pacienteId, profissionalId, Timestamp.from(criadaEm));
    }

    void revogar(long pacienteId, String loginProfissional) {
        banco.update("""
            delete from acessos_acompanhamento
            where paciente_id = ? and profissional_id = (select id from usuarios where login = ?)
            """, pacienteId, loginProfissional);
    }

    boolean temAcesso(long pacienteId, long profissionalId) {
        return Boolean.TRUE.equals(banco.queryForObject("""
            select exists (select 1 from acessos_acompanhamento where paciente_id = ? and profissional_id = ?)
            """, Boolean.class, pacienteId, profissionalId));
    }
}
