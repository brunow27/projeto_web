package br.edu.catolicasc.locafacil.dto;

import br.edu.catolicasc.locafacil.entity.Filial;

public record FilialResponse(Long id, String nome, String cidade, String endereco, String telefone) {

    public static FilialResponse de(Filial f) {
        return new FilialResponse(f.getId(), f.getNome(), f.getCidade(), f.getEndereco(), f.getTelefone());
    }
}
