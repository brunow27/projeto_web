package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.StatusLocacao;
import br.edu.catolicasc.locafacil.entity.Veiculo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    Page<Veiculo> findByAtivoTrue(Pageable pageable);

    /** RF05: impede remover filial que ainda possui veículos. */
    boolean existsByFilialId(Long filialId);

    /** RF04: impede remover categoria que ainda possui veículos. */
    boolean existsByCategoriaId(Long categoriaId);

    /**
     * RF07 / RN01: veículos ativos livres no período (sem locação RESERVADA/ATIVA e sem
     * manutenção que cruze o período). Filtros opcionais: passe null para ignorar.
     */
    default Page<Veiculo> buscarDisponiveis(LocalDateTime inicio, LocalDateTime fim, Long filialId,
                                            Long categoriaId, BigDecimal precoMin, BigDecimal precoMax,
                                            Pageable pageable) {
        return buscarDisponiveisNoPeriodo(inicio, fim, inicio.toLocalDate(), fim.toLocalDate(),
                LocacaoRepository.STATUS_OCUPANTES, filialId, categoriaId, precoMin, precoMax, pageable);
    }

    @Query(value = """
            select v from Veiculo v
            join fetch v.categoria c
            join fetch v.filial f
            where v.ativo = true
              and (:filialId is null or f.id = :filialId)
              and (:categoriaId is null or c.id = :categoriaId)
              and (:precoMin is null or c.valorDiaria >= :precoMin)
              and (:precoMax is null or c.valorDiaria <= :precoMax)
              and not exists (
                    select l.id from Locacao l
                    where l.veiculo = v
                      and l.status in :status
                      and l.retiradaPrevista < :fim
                      and l.devolucaoPrevista > :inicio)
              and not exists (
                    select m.id from Manutencao m
                    where m.veiculo = v
                      and m.dataInicio <= :fimData
                      and m.dataFim >= :inicioData)
            """,
            countQuery = """
            select count(v) from Veiculo v
            join v.categoria c
            join v.filial f
            where v.ativo = true
              and (:filialId is null or f.id = :filialId)
              and (:categoriaId is null or c.id = :categoriaId)
              and (:precoMin is null or c.valorDiaria >= :precoMin)
              and (:precoMax is null or c.valorDiaria <= :precoMax)
              and not exists (
                    select l.id from Locacao l
                    where l.veiculo = v
                      and l.status in :status
                      and l.retiradaPrevista < :fim
                      and l.devolucaoPrevista > :inicio)
              and not exists (
                    select m.id from Manutencao m
                    where m.veiculo = v
                      and m.dataInicio <= :fimData
                      and m.dataFim >= :inicioData)
            """)
    Page<Veiculo> buscarDisponiveisNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim,
                                             @Param("inicioData") LocalDate inicioData,
                                             @Param("fimData") LocalDate fimData,
                                             @Param("status") List<StatusLocacao> status,
                                             @Param("filialId") Long filialId,
                                             @Param("categoriaId") Long categoriaId,
                                             @Param("precoMin") BigDecimal precoMin,
                                             @Param("precoMax") BigDecimal precoMax,
                                             Pageable pageable);
}
