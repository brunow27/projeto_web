package br.edu.catolicasc.locafacil.dto;

import br.edu.catolicasc.locafacil.entity.Opcional;
import java.math.BigDecimal;

public record OpcionalResponse(Long id, String nome, BigDecimal valorDia) {

    public static OpcionalResponse de(Opcional o) {
        return new OpcionalResponse(o.getId(), o.getNome(), o.getValorDia());
    }
}
