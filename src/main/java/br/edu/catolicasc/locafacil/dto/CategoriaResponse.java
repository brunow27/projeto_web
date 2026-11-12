package br.edu.catolicasc.locafacil.dto;

import br.edu.catolicasc.locafacil.entity.Categoria;
import java.math.BigDecimal;

public record CategoriaResponse(Long id, String nome, BigDecimal valorDiaria, BigDecimal valorMultaDia, BigDecimal caucao) {

    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getValorDiaria(), c.getValorMultaDia(), c.getCaucao());
    }
}
