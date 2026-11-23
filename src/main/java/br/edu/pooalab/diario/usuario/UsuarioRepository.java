package br.edu.pooalab.diario.usuario;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);

    // Serializa escritas do mesmo usuário, inclusive a criação do primeiro registro do dia.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.login = :login")
    Optional<Usuario> buscarParaEscrita(@Param("login") String login);
}
