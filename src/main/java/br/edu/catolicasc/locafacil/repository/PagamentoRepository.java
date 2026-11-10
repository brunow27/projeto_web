package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Pagamento;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByLocacaoIdOrderByDataPagamentoAsc(Long locacaoId);

    /** Soma crua; retorna null quando a locação ainda não tem pagamentos. */
    @Query("select sum(p.valor) from Pagamento p where p.locacao.id = :locacaoId")
    BigDecimal somaPagamentos(@Param("locacaoId") Long locacaoId);

    /** RN06: total já pago na locação (nunca nulo). */
    default BigDecimal somarPorLocacao(Long locacaoId) {
        BigDecimal soma = somaPagamentos(locacaoId);
        return soma != null ? soma : BigDecimal.ZERO;
    }
}
