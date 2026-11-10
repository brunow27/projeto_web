package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Locacao;
import br.edu.catolicasc.locafacil.entity.StatusLocacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {

    /** Status que bloqueiam o veículo no período (RN01). */
    List<StatusLocacao> STATUS_OCUPANTES = List.of(StatusLocacao.RESERVADA, StatusLocacao.ATIVA);

    List<Locacao> findByClienteId(Long clienteId);

    List<Locacao> findByStatus(StatusLocacao status);

    /** RF05: impede remover filial com locações associadas. */
    boolean existsByFilialRetiradaIdOrFilialDevolucaoId(Long filialRetiradaId, Long filialDevolucaoId);

    /** Locação com cliente, veículo, categoria, filiais e opcionais numa única consulta (evita N+1). */
    @EntityGraph(attributePaths = {"cliente", "veiculo", "veiculo.categoria", "filialRetirada",
            "filialDevolucao", "opcionais", "opcionais.opcional"})
    @Query("select l from Locacao l where l.id = :id")
    Optional<Locacao> findDetalhadaById(@Param("id") Long id);

    /** RN01: o veículo já tem locação (RESERVADA/ATIVA) que cruza o período? */
    @Query("""
            select case when count(l) > 0 then true else false end
            from Locacao l
            where l.veiculo.id = :veiculoId
              and l.status in :status
              and l.retiradaPrevista < :fim
              and l.devolucaoPrevista > :inicio
            """)
    boolean existeConflito(@Param("veiculoId") Long veiculoId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim,
                           @Param("status") List<StatusLocacao> status);

    default boolean existeConflito(Long veiculoId, LocalDateTime inicio, LocalDateTime fim) {
        return existeConflito(veiculoId, inicio, fim, STATUS_OCUPANTES);
    }
}
