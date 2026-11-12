package br.edu.catolicasc.locafacil.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Envelope estável para listagens paginadas (RNF09). */
public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
