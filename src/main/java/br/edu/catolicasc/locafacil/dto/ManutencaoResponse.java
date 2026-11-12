package br.edu.catolicasc.locafacil.dto;

import br.edu.catolicasc.locafacil.entity.Manutencao;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ManutencaoResponse(Long id, Long veiculoId, String placa, String tipo,
                                 LocalDate dataInicio, LocalDate dataFim, BigDecimal custo) {

    public static ManutencaoResponse de(Manutencao m) {
        return new ManutencaoResponse(m.getId(), m.getVeiculo().getId(), m.getVeiculo().getPlaca(),
                m.getTipo(), m.getDataInicio(), m.getDataFim(), m.getCusto());
    }
}
