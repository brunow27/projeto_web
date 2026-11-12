package br.edu.catolicasc.locafacil.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.catolicasc.locafacil.dto.VeiculoRequest;
import br.edu.catolicasc.locafacil.dto.VeiculoResponse;
import br.edu.catolicasc.locafacil.entity.Categoria;
import br.edu.catolicasc.locafacil.entity.Filial;
import br.edu.catolicasc.locafacil.entity.StatusVeiculo;
import br.edu.catolicasc.locafacil.entity.Veiculo;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.exception.RegraNegocioException;
import br.edu.catolicasc.locafacil.repository.CategoriaRepository;
import br.edu.catolicasc.locafacil.repository.FilialRepository;
import br.edu.catolicasc.locafacil.repository.LocacaoRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock VeiculoRepository veiculos;
    @Mock CategoriaRepository categorias;
    @Mock FilialRepository filiais;
    @Mock LocacaoRepository locacoes;
    @InjectMocks VeiculoService service;

    @Test
    void criar_normalizaPlacaEAssumeStatusDisponivel() {
        stubCategoriaEFilial();
        when(veiculos.save(any(Veiculo.class))).thenAnswer(i -> i.getArgument(0));

        VeiculoResponse resposta = service.criar(request(" abc1d23 ", null));

        assertThat(resposta.placa()).isEqualTo("ABC1D23");
        assertThat(resposta.status()).isEqualTo(StatusVeiculo.DISPONIVEL);
        assertThat(resposta.ativo()).isTrue();
    }

    @Test
    void criar_comPlacaJaCadastrada_lancaConflito() {
        when(veiculos.findByPlacaIgnoreCase("ABC1D23")).thenReturn(Optional.of(veiculo(1L)));

        assertThatThrownBy(() -> service.criar(request("ABC1D23", null))).isInstanceOf(ConflitoException.class);
        verify(veiculos, never()).save(any());
    }

    @Test
    void criar_comStatusAlugado_lancaRegraNegocio() {
        assertThatThrownBy(() -> service.criar(request("ABC1D23", StatusVeiculo.ALUGADO)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void criar_comCategoriaInexistente_lancaNaoEncontrado() {
        when(categorias.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("ABC1D23", null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizar_comPlacaDeOutroVeiculo_lancaConflito() {
        when(veiculos.findById(2L)).thenReturn(Optional.of(veiculo(2L)));
        when(veiculos.findByPlacaIgnoreCase("ABC1D23")).thenReturn(Optional.of(veiculo(1L)));

        assertThatThrownBy(() -> service.atualizar(2L, request("ABC1D23", null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void remover_comLocacaoEmAndamento_lancaConflito() {
        Veiculo v = veiculo(1L);
        when(veiculos.findById(1L)).thenReturn(Optional.of(v));
        when(locacoes.existsByVeiculoIdAndStatusIn(1L, LocacaoRepository.STATUS_OCUPANTES)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L)).isInstanceOf(ConflitoException.class);
        assertThat(v.isAtivo()).isTrue();
    }

    @Test
    void remover_semLocacaoEmAndamento_fazExclusaoLogica() {
        Veiculo v = veiculo(1L);
        when(veiculos.findById(1L)).thenReturn(Optional.of(v));

        service.remover(1L);

        assertThat(v.isAtivo()).isFalse();
        verify(veiculos, never()).delete(any());
    }

    @Test
    void disponiveis_comPeriodoInvertido_lancaRegraNegocio() {
        LocalDateTime inicio = LocalDateTime.of(2026, 12, 4, 10, 0);

        assertThatThrownBy(() -> service.disponiveis(inicio, inicio.minusDays(1), null, null, null, null,
                Pageable.unpaged())).isInstanceOf(RegraNegocioException.class);
    }

    private void stubCategoriaEFilial() {
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria()));
        when(filiais.findById(2L)).thenReturn(Optional.of(filial()));
    }

    private static VeiculoRequest request(String placa, StatusVeiculo status) {
        return new VeiculoRequest(placa, "Fiat", "Argo", 2024, 1000, status, 1L, 2L);
    }

    private static Categoria categoria() {
        Categoria c = new Categoria();
        c.setNome("Econômico");
        c.setValorDiaria(new BigDecimal("100.00"));
        c.setValorMultaDia(new BigDecimal("50.00"));
        c.setCaucao(new BigDecimal("500.00"));
        return c;
    }

    private static Filial filial() {
        Filial f = new Filial();
        f.setNome("Centro");
        f.setCidade("Jaraguá do Sul");
        return f;
    }

    private static Veiculo veiculo(Long id) {
        Veiculo v = new Veiculo();
        v.setPlaca("ABC1D23");
        ReflectionTestUtils.setField(v, "id", id);
        return v;
    }
}
