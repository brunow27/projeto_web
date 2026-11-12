package br.edu.catolicasc.locafacil.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.catolicasc.locafacil.dto.CategoriaRequest;
import br.edu.catolicasc.locafacil.dto.CategoriaResponse;
import br.edu.catolicasc.locafacil.entity.Categoria;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.repository.CategoriaRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    static final CategoriaRequest SUV = new CategoriaRequest(
            "SUV", new BigDecimal("200.00"), new BigDecimal("80.00"), new BigDecimal("1000.00"));

    @Mock CategoriaRepository categorias;
    @Mock VeiculoRepository veiculos;
    @InjectMocks CategoriaService service;

    @Test
    void criar_salvaCategoriaNova() {
        when(categorias.findByNomeIgnoreCase("SUV")).thenReturn(Optional.empty());
        when(categorias.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        CategoriaResponse resposta = service.criar(SUV);

        assertThat(resposta.nome()).isEqualTo("SUV");
        assertThat(resposta.valorDiaria()).isEqualByComparingTo("200.00");
    }

    @Test
    void criar_comNomeJaExistente_lancaConflito() {
        when(categorias.findByNomeIgnoreCase("SUV")).thenReturn(Optional.of(categoria(1L)));

        assertThatThrownBy(() -> service.criar(SUV)).isInstanceOf(ConflitoException.class);
        verify(categorias, never()).save(any());
    }

    @Test
    void atualizar_comNomeDeOutraCategoria_lancaConflito() {
        when(categorias.findById(2L)).thenReturn(Optional.of(categoria(2L)));
        when(categorias.findByNomeIgnoreCase("SUV")).thenReturn(Optional.of(categoria(1L)));

        assertThatThrownBy(() -> service.atualizar(2L, SUV)).isInstanceOf(ConflitoException.class);
    }

    @Test
    void atualizar_mantendoOProprioNome_alteraOsValores() {
        Categoria atual = categoria(1L);
        when(categorias.findById(1L)).thenReturn(Optional.of(atual));
        when(categorias.findByNomeIgnoreCase("SUV")).thenReturn(Optional.of(atual));

        CategoriaResponse resposta = service.atualizar(1L, SUV);

        assertThat(resposta.valorDiaria()).isEqualByComparingTo("200.00");
        assertThat(atual.getCaucao()).isEqualByComparingTo("1000.00");
    }

    @Test
    void remover_comVeiculosAssociados_lancaConflito() {
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria(1L)));
        when(veiculos.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L)).isInstanceOf(ConflitoException.class);
        verify(categorias, never()).delete(any());
    }

    @Test
    void remover_semVeiculos_exclui() {
        Categoria categoria = categoria(1L);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(veiculos.existsByCategoriaId(1L)).thenReturn(false);

        service.remover(1L);

        verify(categorias).delete(categoria);
    }

    @Test
    void buscar_inexistente_lancaNaoEncontrado() {
        when(categorias.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private static Categoria categoria(Long id) {
        Categoria c = new Categoria();
        c.setNome("SUV");
        c.setValorDiaria(new BigDecimal("100.00"));
        c.setValorMultaDia(new BigDecimal("50.00"));
        c.setCaucao(new BigDecimal("500.00"));
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }
}
