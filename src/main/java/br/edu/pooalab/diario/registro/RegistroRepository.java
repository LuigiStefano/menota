package br.edu.pooalab.diario.registro;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    Optional<Registro> findByUsuarioLoginAndData(String login, LocalDate data);
    List<Registro> findByUsuarioLoginAndDataBetweenOrderByDataAsc(String login, LocalDate inicio, LocalDate fim);
}
