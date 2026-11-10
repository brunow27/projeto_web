package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Habilitacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabilitacaoRepository extends JpaRepository<Habilitacao, Long> {

    Optional<Habilitacao> findByNumeroCnh(String numeroCnh);

    boolean existsByNumeroCnh(String numeroCnh);
}
