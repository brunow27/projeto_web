package br.edu.catolicasc.locafacil.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.catolicasc.locafacil.dto.OpcionalRequest;
import br.edu.catolicasc.locafacil.entity.Opcional;
import br.edu.catolicasc.locafacil.exception.ConflitoException;
import br.edu.catolicasc.locafacil.repository.LocacaoOpcionalRepository;
import br.edu.catolicasc.locafacil.repository.OpcionalRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpcionalServiceTest {

    @Mock OpcionalRepository opcionais;
    @Mock LocacaoOpcionalRepository locacaoOpcionais;
    @InjectMocks OpcionalService service;

    @Test
    void criar_comNomeJaExistente_lancaConflito() {
        when(opcionais.findByNomeIgnoreCase("GPS")).thenReturn(Optional.of(new Opcional()));

        assertThatThrownBy(() -> service.criar(new OpcionalRequest("GPS", new BigDecimal("10.00"))))
                .isInstanceOf(ConflitoException.class);
        verify(opcionais, never()).save(any());
    }

    @Test
    void remover_jaUsadoEmLocacao_lancaConflito() {
        when(opcionais.findById(1L)).thenReturn(Optional.of(new Opcional()));
        when(locacaoOpcionais.existsByOpcionalId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L)).isInstanceOf(ConflitoException.class);
        verify(opcionais, never()).delete(any());
    }

    @Test
    void remover_naoUsado_exclui() {
        Opcional opcional = new Opcional();
        when(opcionais.findById(1L)).thenReturn(Optional.of(opcional));
        when(locacaoOpcionais.existsByOpcionalId(1L)).thenReturn(false);

        service.remover(1L);

        verify(opcionais).delete(opcional);
    }
}
