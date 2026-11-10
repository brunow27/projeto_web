package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Opcional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionalRepository extends JpaRepository<Opcional, Long> {

    Optional<Opcional> findByNomeIgnoreCase(String nome);
}
