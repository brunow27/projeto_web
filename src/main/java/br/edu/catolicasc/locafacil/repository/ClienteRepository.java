package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Cliente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    Optional<Cliente> findByUsuarioId(Long usuarioId);

    /** Carrega o cliente já com a habilitação (evita N+1). */
    @EntityGraph(attributePaths = "habilitacao")
    Optional<Cliente> findWithHabilitacaoById(Long id);

    /** Busca paginada por parte do nome (RF03). */
    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
