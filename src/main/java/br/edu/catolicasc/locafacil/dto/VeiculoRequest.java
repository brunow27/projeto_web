package br.edu.catolicasc.locafacil.dto;

import br.edu.catolicasc.locafacil.entity.StatusVeiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** "status" é opcional: na criação assume DISPONIVEL. ALUGADO é definido apenas pela retirada da locação. */
public record VeiculoRequest(
        @NotBlank(message = "A placa é obrigatória")
        @Pattern(regexp = "^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$", message = "Placa inválida") String placa,
        @NotBlank(message = "A marca é obrigatória") @Size(max = 60) String marca,
        @NotBlank(message = "O modelo é obrigatório") @Size(max = 60) String modelo,
        @NotNull(message = "O ano é obrigatório") @Min(1900) Integer ano,
        @NotNull(message = "A quilometragem é obrigatória") @Min(0) Integer quilometragem,
        StatusVeiculo status,
        @NotNull(message = "A categoria é obrigatória") Long categoriaId,
        @NotNull(message = "A filial é obrigatória") Long filialId) {
}
