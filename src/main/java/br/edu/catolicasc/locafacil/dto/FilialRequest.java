package br.edu.catolicasc.locafacil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FilialRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 80) String nome,
        @NotBlank(message = "A cidade é obrigatória") @Size(max = 80) String cidade,
        @Size(max = 200) String endereco,
        @Size(max = 20) String telefone) {
}
