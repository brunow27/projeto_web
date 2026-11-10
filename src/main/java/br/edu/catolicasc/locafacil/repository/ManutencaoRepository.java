package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Manutencao;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    List<Manutencao> findByVeiculoIdOrderByDataInicioDesc(Long veiculoId);

    /** RN01: existe manutenção do veículo que cruza o período [inicio, fim] (datas inclusivas)? */
    @Query("""
            select case when count(m) > 0 then true else false end
            from Manutencao m
            where m.veiculo.id = :veiculoId
              and m.dataInicio <= :fim
              and m.dataFim >= :inicio
            """)
    boolean existeSobreposicao(@Param("veiculoId") Long veiculoId,
                               @Param("inicio") LocalDate inicio,
                               @Param("fim") LocalDate fim);
}
