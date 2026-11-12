package br.edu.catolicasc.locafacil.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ManutencaoRequest(
        @NotNull(message = "O veículo é obrigatório") Long veiculoId,
        @NotBlank(message = "O tipo é obrigatório") @Size(max = 60) String tipo,
        @NotNull(message = "A data inicial é obrigatória") LocalDate dataInicio,
        @NotNull(message = "A data final é obrigatória") LocalDate dataFim,
        @NotNull(message = "O custo é obrigatório") @DecimalMin("0.00") BigDecimal custo) {
}
