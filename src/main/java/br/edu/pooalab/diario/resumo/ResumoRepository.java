package br.edu.pooalab.diario.resumo;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface ResumoRepository extends JpaRepository<Resumo, UUID> {
    List<Resumo> findTop20ByUsuarioLoginOrderByCriadoEmDesc(String login);
    Optional<Resumo> findByIdAndUsuarioLogin(UUID id, String login);
    Optional<Resumo> findByUsuarioLoginAndInicioAndFimAndAssinatura(String login, LocalDate inicio, LocalDate fim, String assinatura);

    @Modifying
    @Query("delete from Resumo r where r.usuario.login = :login and r.inicio <= :data and r.fim >= :data")
    void removerDoPeriodoAlterado(@Param("login") String login, @Param("data") LocalDate data);
}
