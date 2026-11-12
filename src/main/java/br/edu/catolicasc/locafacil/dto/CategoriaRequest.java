package br.edu.catolicasc.locafacil.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CategoriaRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 60) String nome,
        @NotNull(message = "A diária é obrigatória")
        @DecimalMin(value = "0.00", inclusive = false, message = "A diária deve ser maior que zero") BigDecimal valorDiaria,
        @NotNull(message = "A multa por dia é obrigatória") @DecimalMin("0.00") BigDecimal valorMultaDia,
        @NotNull(message = "A caução é obrigatória") @DecimalMin("0.00") BigDecimal caucao) {
}
