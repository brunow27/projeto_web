package br.edu.catolicasc.locafacil.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.catolicasc.locafacil.dto.ManutencaoRequest;
import br.edu.catolicasc.locafacil.dto.ManutencaoResponse;
import br.edu.catolicasc.locafacil.entity.Manutencao;
import br.edu.catolicasc.locafacil.entity.Veiculo;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.exception.RecursoNaoEncontradoException;
import br.edu.catolicasc.locafacil.exception.RegraNegocioException;
import br.edu.catolicasc.locafacil.repository.LocacaoRepository;
import br.edu.catolicasc.locafacil.repository.ManutencaoRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    static final LocalDate INICIO = LocalDate.of(2026, 12, 2);
    static final LocalDate FIM = LocalDate.of(2026, 12, 3);

    @Mock ManutencaoRepository manutencoes;
    @Mock VeiculoRepository veiculos;
    @Mock LocacaoRepository locacoes;
    @InjectMocks ManutencaoService service;

    @Test
    void criar_salvaManutencaoDoVeiculo() {
        Veiculo veiculo = veiculo();
        when(veiculos.findById(1L)).thenReturn(Optional.of(veiculo));
        when(manutencoes.save(any(Manutencao.class))).thenAnswer(i -> i.getArgument(0));

        ManutencaoResponse resposta = service.criar(request(INICIO, FIM));

        assertThat(resposta.placa()).isEqualTo("ABC1D23");
        assertThat(resposta.tipo()).isEqualTo("Revisão");
        assertThat(resposta.custo()).isEqualByComparingTo("300.00");
    }

    @Test
    void criar_comVeiculoInexistente_lancaNaoEncontrado() {
        when(veiculos.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(INICIO, FIM)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void criar_comDataFinalAnteriorAInicial_lancaRegraNegocio() {
        when(veiculos.findById(1L)).thenReturn(Optional.of(veiculo()));

        assertThatThrownBy(() -> service.criar(request(FIM, INICIO))).isInstanceOf(RegraNegocioException.class);
        verify(manutencoes, never()).save(any());
    }

    @Test
    void criar_noPeriodoDeUmaLocacao_lancaConflito() {
        when(veiculos.findById(1L)).thenReturn(Optional.of(veiculo()));
        when(locacoes.existeConflito(eq(1L), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request(INICIO, FIM))).isInstanceOf(ConflitoException.class);
        verify(manutencoes, never()).save(any());
    }

    private static ManutencaoRequest request(LocalDate inicio, LocalDate fim) {
        return new ManutencaoRequest(1L, "Revisão", inicio, fim, new BigDecimal("300.00"));
    }

    private static Veiculo veiculo() {
        Veiculo v = new Veiculo();
        v.setPlaca("ABC1D23");
        return v;
    }
}
