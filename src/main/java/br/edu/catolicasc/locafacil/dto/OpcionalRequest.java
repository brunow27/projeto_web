package br.edu.catolicasc.locafacil.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OpcionalRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 60) String nome,
        @NotNull(message = "O valor por dia é obrigatório") @DecimalMin("0.00") BigDecimal valorDia) {
}
