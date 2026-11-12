package br.edu.catolicasc.locafacil.controller;

import br.edu.catolicasc.locafacil.dto.OpcionalRequest;
import br.edu.catolicasc.locafacil.dto.OpcionalResponse;
import br.edu.catolicasc.locafacil.dto.PaginaResponse;
import br.edu.catolicasc.locafacil.service.OpcionalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opcionais")
public class OpcionalController {

    private final OpcionalService service;

    public OpcionalController(OpcionalService service) {
        this.service = service;
    }

    @GetMapping
    public PaginaResponse<OpcionalResponse> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    public OpcionalResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpcionalResponse criar(@Valid @RequestBody OpcionalRequest dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public OpcionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody OpcionalRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
