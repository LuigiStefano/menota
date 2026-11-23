package br.edu.pooalab.diario.registro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    Optional<Registro> findByUsuarioLoginAndData(String login, LocalDate data);
    List<Registro> findByUsuarioLoginAndDataBetweenOrderByDataAsc(String login, LocalDate inicio, LocalDate fim);
    @Query("select r.data from Registro r where r.usuario.login = :login and r.data between :inicio and :fim order by r.data")
    List<LocalDate> datasDoPeriodo(String login, LocalDate inicio, LocalDate fim);
}
