package br.edu.catolicasc.locafacil.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.catolicasc.locafacil.entity.Filial;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.repository.FilialRepository;
import br.edu.catolicasc.locafacil.repository.LocacaoRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilialServiceTest {

    @Mock FilialRepository filiais;
    @Mock VeiculoRepository veiculos;
    @Mock LocacaoRepository locacoes;
    @InjectMocks FilialService service;

    @Test
    void remover_comVeiculos_lancaConflito() {
        when(filiais.findById(1L)).thenReturn(Optional.of(new Filial()));
        when(veiculos.existsByFilialId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L)).isInstanceOf(ConflitoException.class);
        verify(filiais, never()).delete(any());
    }

    @Test
    void remover_comLocacoes_lancaConflito() {
        when(filiais.findById(1L)).thenReturn(Optional.of(new Filial()));
        when(veiculos.existsByFilialId(1L)).thenReturn(false);
        when(locacoes.existsByFilialRetiradaIdOrFilialDevolucaoId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L)).isInstanceOf(ConflitoException.class);
        verify(filiais, never()).delete(any());
    }

    @Test
    void remover_semVinculos_exclui() {
        Filial filial = new Filial();
        when(filiais.findById(1L)).thenReturn(Optional.of(filial));
        when(veiculos.existsByFilialId(1L)).thenReturn(false);
        when(locacoes.existsByFilialRetiradaIdOrFilialDevolucaoId(1L, 1L)).thenReturn(false);

        service.remover(1L);

        verify(filiais).delete(filial);
    }
}
