package br.edu.catolicasc.locafacil.service;

import br.edu.catolicasc.locafacil.dto.CategoriaRequest;
import br.edu.catolicasc.locafacil.dto.CategoriaResponse;
import br.edu.catolicasc.locafacil.dto.PaginaResponse;
import br.edu.catolicasc.locafacil.entity.Categoria;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.repository.CategoriaRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final VeiculoRepository veiculos;

    public CategoriaService(CategoriaRepository categorias, VeiculoRepository veiculos) {
        this.categorias = categorias;
        this.veiculos = veiculos;
    }

    public PaginaResponse<CategoriaResponse> listar(Pageable pageable) {
        return PaginaResponse.de(categorias.findAll(pageable).map(CategoriaResponse::de));
    }

    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(obter(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest dados) {
        validarNomeUnico(dados.nome(), null);
        Categoria categoria = new Categoria();
        preencher(categoria, dados);
        return CategoriaResponse.de(categorias.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest dados) {
        Categoria categoria = obter(id);
        validarNomeUnico(dados.nome(), id);
        preencher(categoria, dados);
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void remover(Long id) {
        Categoria categoria = obter(id);
        if (veiculos.existsByCategoriaId(id)) {
            throw new ConflitoException("Categoria possui veículos associados");
        }
        categorias.delete(categoria);
    }

    private Categoria obter(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private void validarNomeUnico(String nome, Long idIgnorado) {
        categorias.findByNomeIgnoreCase(nome)
                .filter(outra -> !Objects.equals(outra.getId(), idIgnorado))
                .ifPresent(outra -> {
                    throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'");
                });
    }

    private void preencher(Categoria categoria, CategoriaRequest dados) {
        categoria.setNome(dados.nome());
        categoria.setValorDiaria(dados.valorDiaria());
        categoria.setValorMultaDia(dados.valorMultaDia());
        categoria.setCaucao(dados.caucao());
    }
}
