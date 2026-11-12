package br.edu.catolicasc.locafacil.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/** Corpo padrão de erro da API (RNF07). "campos" só aparece em erros de validação. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(LocalDateTime timestamp, int status, String erro, String mensagem,
                       String caminho, Map<String, String> campos) {
}
