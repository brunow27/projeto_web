package br.edu.catolicasc.locafacil.dto;

import br.edu.catolicasc.locafacil.entity.StatusVeiculo;
import br.edu.catolicasc.locafacil.entity.Veiculo;

public record VeiculoResponse(Long id, String placa, String marca, String modelo, int ano, int quilometragem,
                              StatusVeiculo status, boolean ativo, CategoriaResponse categoria, FilialResponse filial) {

    public static VeiculoResponse de(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAno(),
                v.getQuilometragem(), v.getStatus(), v.isAtivo(),
                CategoriaResponse.de(v.getCategoria()), FilialResponse.de(v.getFilial()));
    }
}
