package br.edu.catolicasc.locafacil.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.catolicasc.locafacil.dto.PaginaResponse;
import br.edu.catolicasc.locafacil.service.VeiculoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VeiculoController.class)
class VeiculoControllerTest {

    @Autowired MockMvc mvc;
    @MockBean VeiculoService service;

    @Test
    void disponiveis_semPeriodo_retorna400() throws Exception {
        mvc.perform(get("/veiculos/disponiveis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Parâmetro obrigatório ausente: inicio"));
    }

    @Test
    void disponiveis_comDataEmFormatoInvalido_retorna400() throws Exception {
        mvc.perform(get("/veiculos/disponiveis").param("inicio", "amanhã").param("fim", "2026-12-04T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disponiveis_comPeriodoValido_retorna200() throws Exception {
        when(service.disponiveis(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));

        mvc.perform(get("/veiculos/disponiveis")
                        .param("inicio", "2026-12-01T10:00:00").param("fim", "2026-12-04T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void criar_comDadosInvalidos_retorna400ComErroPorCampo() throws Exception {
        mvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"123\",\"marca\":\"Fiat\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.placa").exists())
                .andExpect(jsonPath("$.campos.modelo").exists())
                .andExpect(jsonPath("$.campos.categoriaId").exists())
                .andExpect(jsonPath("$.campos.filialId").exists());
    }
}
