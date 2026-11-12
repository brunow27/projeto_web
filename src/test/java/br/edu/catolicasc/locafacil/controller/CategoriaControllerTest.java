package br.edu.catolicasc.locafacil.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.catolicasc.locafacil.dto.CategoriaResponse;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.service.CategoriaService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Valida o contrato HTTP (status e corpo de erro padronizado) sem subir banco. */
@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    static final String JSON_VALIDO = """
            {"nome":"SUV","valorDiaria":200.00,"valorMultaDia":80.00,"caucao":1000.00}""";

    @Autowired MockMvc mvc;
    @MockBean CategoriaService service;

    @Test
    void criar_comDadosValidos_retorna201() throws Exception {
        when(service.criar(any())).thenReturn(new CategoriaResponse(1L, "SUV",
                new BigDecimal("200.00"), new BigDecimal("80.00"), new BigDecimal("1000.00")));

        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("SUV"));
    }

    @Test
    void criar_comDadosInvalidos_retorna400ComErroPorCampo() throws Exception {
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"valorDiaria\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.valorDiaria").exists())
                .andExpect(jsonPath("$.campos.caucao").exists());
    }

    @Test
    void criar_comJsonMalFormatado_retorna400() throws Exception {
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("{nome:"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_comNomeDuplicado_retorna409() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("Já existe uma categoria com o nome 'SUV'"));

        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe uma categoria com o nome 'SUV'"))
                .andExpect(jsonPath("$.caminho").value("/categorias"));
    }

    @Test
    void buscar_inexistente_retorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Categoria", 99L));

        mvc.perform(get("/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void buscar_comIdNaoNumerico_retorna400() throws Exception {
        mvc.perform(get("/categorias/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void remover_retorna204() throws Exception {
        mvc.perform(delete("/categorias/1")).andExpect(status().isNoContent());
    }
}
