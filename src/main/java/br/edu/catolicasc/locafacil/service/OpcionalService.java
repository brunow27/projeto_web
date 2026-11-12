package br.edu.catolicasc.locafacil.service;

import br.edu.catolicasc.locafacil.dto.OpcionalRequest;
import br.edu.catolicasc.locafacil.dto.OpcionalResponse;
import br.edu.catolicasc.locafacil.dto.PaginaResponse;
import br.edu.catolicasc.locafacil.entity.Opcional;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.repository.LocacaoOpcionalRepository;
import br.edu.catolicasc.locafacil.repository.OpcionalRepository;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OpcionalService {

    private final OpcionalRepository opcionais;
    private final LocacaoOpcionalRepository locacaoOpcionais;

    public OpcionalService(OpcionalRepository opcionais, LocacaoOpcionalRepository locacaoOpcionais) {
        this.opcionais = opcionais;
        this.locacaoOpcionais = locacaoOpcionais;
    }

    public PaginaResponse<OpcionalResponse> listar(Pageable pageable) {
        return PaginaResponse.de(opcionais.findAll(pageable).map(OpcionalResponse::de));
    }

    public OpcionalResponse buscar(Long id) {
        return OpcionalResponse.de(obter(id));
    }

    @Transactional
    public OpcionalResponse criar(OpcionalRequest dados) {
        validarNomeUnico(dados.nome(), null);
        Opcional opcional = new Opcional();
        preencher(opcional, dados);
        return OpcionalResponse.de(opcionais.save(opcional));
    }

    @Transactional
    public OpcionalResponse atualizar(Long id, OpcionalRequest dados) {
        Opcional opcional = obter(id);
        validarNomeUnico(dados.nome(), id);
        preencher(opcional, dados);
        return OpcionalResponse.de(opcional);
    }

    @Transactional
    public void remover(Long id) {
        Opcional opcional = obter(id);
        if (locacaoOpcionais.existsByOpcionalId(id)) {
            throw new ConflitoException("Opcional já foi usado em locações");
        }
        opcionais.delete(opcional);
    }

    private Opcional obter(Long id) {
        return opcionais.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Opcional", id));
    }

    private void validarNomeUnico(String nome, Long idIgnorado) {
        opcionais.findByNomeIgnoreCase(nome)
                .filter(outro -> !Objects.equals(outro.getId(), idIgnorado))
                .ifPresent(outro -> {
                    throw new ConflitoException("Já existe um opcional com o nome '" + nome + "'");
                });
    }

    private void preencher(Opcional opcional, OpcionalRequest dados) {
        opcional.setNome(dados.nome());
        opcional.setValorDia(dados.valorDia());
    }
}
