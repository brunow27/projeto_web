package br.edu.catolicasc.locafacil.service;

import br.edu.catolicasc.locafacil.dto.FilialRequest;
import br.edu.catolicasc.locafacil.dto.FilialResponse;
import br.edu.catolicasc.locafacil.dto.PaginaResponse;
import br.edu.catolicasc.locafacil.entity.Filial;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.repository.FilialRepository;
import br.edu.catolicasc.locafacil.repository.LocacaoRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FilialService {

    private final FilialRepository filiais;
    private final VeiculoRepository veiculos;
    private final LocacaoRepository locacoes;

    public FilialService(FilialRepository filiais, VeiculoRepository veiculos, LocacaoRepository locacoes) {
        this.filiais = filiais;
        this.veiculos = veiculos;
        this.locacoes = locacoes;
    }

    public PaginaResponse<FilialResponse> listar(Pageable pageable) {
        return PaginaResponse.de(filiais.findAll(pageable).map(FilialResponse::de));
    }

    public FilialResponse buscar(Long id) {
        return FilialResponse.de(obter(id));
    }

    @Transactional
    public FilialResponse criar(FilialRequest dados) {
        Filial filial = new Filial();
        preencher(filial, dados);
        return FilialResponse.de(filiais.save(filial));
    }

    @Transactional
    public FilialResponse atualizar(Long id, FilialRequest dados) {
        Filial filial = obter(id);
        preencher(filial, dados);
        return FilialResponse.de(filial);
    }

    /** RF05: não remove filial com veículos ou locações associados. */
    @Transactional
    public void remover(Long id) {
        Filial filial = obter(id);
        if (veiculos.existsByFilialId(id) || locacoes.existsByFilialRetiradaIdOrFilialDevolucaoId(id, id)) {
            throw new ConflitoException("Filial possui veículos ou locações associados");
        }
        filiais.delete(filial);
    }

    private Filial obter(Long id) {
        return filiais.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Filial", id));
    }

    private void preencher(Filial filial, FilialRequest dados) {
        filial.setNome(dados.nome());
        filial.setCidade(dados.cidade());
        filial.setEndereco(dados.endereco());
        filial.setTelefone(dados.telefone());
    }
}
