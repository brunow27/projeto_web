package br.edu.catolicasc.locafacil;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.catolicasc.locafacil.entity.Categoria;
import br.edu.catolicasc.locafacil.entity.Cliente;
import br.edu.catolicasc.locafacil.entity.Filial;
import br.edu.catolicasc.locafacil.entity.FormaPagamento;
import br.edu.catolicasc.locafacil.entity.Habilitacao;
import br.edu.catolicasc.locafacil.entity.Locacao;
import br.edu.catolicasc.locafacil.entity.LocacaoOpcional;
import br.edu.catolicasc.locafacil.entity.Manutencao;
import br.edu.catolicasc.locafacil.entity.Opcional;
import br.edu.catolicasc.locafacil.entity.Pagamento;
import br.edu.catolicasc.locafacil.entity.StatusLocacao;
import br.edu.catolicasc.locafacil.entity.StatusVeiculo;
import br.edu.catolicasc.locafacil.entity.TipoPagamento;
import br.edu.catolicasc.locafacil.entity.Veiculo;
import br.edu.catolicasc.locafacil.repository.CategoriaRepository;
import br.edu.catolicasc.locafacil.repository.ClienteRepository;
import br.edu.catolicasc.locafacil.repository.FilialRepository;
import br.edu.catolicasc.locafacil.repository.HabilitacaoRepository;
import br.edu.catolicasc.locafacil.repository.LocacaoOpcionalRepository;
import br.edu.catolicasc.locafacil.repository.LocacaoRepository;
import br.edu.catolicasc.locafacil.repository.ManutencaoRepository;
import br.edu.catolicasc.locafacil.repository.OpcionalRepository;
import br.edu.catolicasc.locafacil.repository.PagamentoRepository;
import br.edu.catolicasc.locafacil.repository.VeiculoRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/** Testes da camada de persistência (H2 em memória). Cada teste roda em transação com rollback. */
@DataJpaTest
class PersistenciaTest {

    @Autowired EntityManager em;
    @Autowired CategoriaRepository categorias;
    @Autowired FilialRepository filiais;
    @Autowired VeiculoRepository veiculos;
    @Autowired ClienteRepository clientes;
    @Autowired HabilitacaoRepository habilitacoes;
    @Autowired OpcionalRepository opcionais;
    @Autowired LocacaoRepository locacoes;
    @Autowired LocacaoOpcionalRepository locacaoOpcionais;
    @Autowired PagamentoRepository pagamentos;
    @Autowired ManutencaoRepository manutencoes;

    // Período padrão: 01/12 10:00 → 04/12 10:00 = 3 diárias
    static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 1, 10, 0);
    static final LocalDateTime FIM = LocalDateTime.of(2026, 12, 4, 10, 0);

    Categoria categoria;   // diária 100,00 · multa/dia 50,00
    Filial filial;
    Cliente cliente;

    @BeforeEach
    void preparar() {
        categoria = new Categoria();
        categoria.setNome("Econômico");
        categoria.setValorDiaria(new BigDecimal("100.00"));
        categoria.setValorMultaDia(new BigDecimal("50.00"));
        categoria.setCaucao(new BigDecimal("500.00"));
        categoria = categorias.save(categoria);

        filial = new Filial();
        filial.setNome("Centro");
        filial.setCidade("Jaraguá do Sul");
        filial = filiais.save(filial);

        cliente = novoCliente("12345678901", "CNH0001", LocalDate.of(2030, 1, 1));
    }

    // ------------------------------------------------------------------ helpers

    Cliente novoCliente(String cpf, String cnh, LocalDate validade) {
        Habilitacao h = new Habilitacao();
        h.setNumeroCnh(cnh);
        h.setCategoria("B");
        h.setValidade(validade);
        Cliente c = new Cliente();
        c.setNome("Cliente " + cpf);
        c.setCpf(cpf);
        c.setHabilitacao(h);
        return clientes.save(c);
    }

    Veiculo novoVeiculo(String placa) {
        Veiculo v = new Veiculo();
        v.setPlaca(placa);
        v.setMarca("Fiat");
        v.setModelo("Argo");
        v.setAno(2024);
        v.setQuilometragem(1000);
        v.setCategoria(categoria);
        v.setFilial(filial);
        return veiculos.save(v);
    }

    Locacao novaLocacao(Veiculo v, LocalDateTime ini, LocalDateTime fim) {
        Locacao l = new Locacao();
        l.setCliente(cliente);
        l.setVeiculo(v);
        l.setFilialRetirada(filial);
        l.setFilialDevolucao(filial);
        l.setRetiradaPrevista(ini);
        l.setDevolucaoPrevista(fim);
        l.setValorTotal(l.calcularValorPrevisto());
        return locacoes.save(l);
    }

    void limparCache() {
        em.flush();
        em.clear();
    }

    // ------------------------------------------------------------------ testes

    @Test
    void cascade1para1_salvaHabilitacaoJuntoComCliente() {
        limparCache();

        assertThat(habilitacoes.findByNumeroCnh("CNH0001")).isPresent();
        Cliente recarregado = clientes.findWithHabilitacaoById(cliente.getId()).orElseThrow();
        assertThat(recarregado.getHabilitacao().getValidade()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(recarregado.possuiCnhValida()).isTrue();
    }

    @Test
    void orphanRemoval_removeHabilitacaoSubstituida() {
        Habilitacao nova = new Habilitacao();
        nova.setNumeroCnh("CNH0002");
        nova.setCategoria("AB");
        nova.setValidade(LocalDate.of(2031, 1, 1));
        cliente.setHabilitacao(nova);
        limparCache();

        assertThat(habilitacoes.findByNumeroCnh("CNH0001")).isEmpty();
        assertThat(habilitacoes.findByNumeroCnh("CNH0002")).isPresent();
    }

    @Test
    void relacaoNparaM_gravaOpcionaisComValorCongelado() {
        Opcional gps = new Opcional();
        gps.setNome("GPS");
        gps.setValorDia(new BigDecimal("10.00"));
        gps = opcionais.save(gps);

        Veiculo v = novoVeiculo("AAA1A11");
        Locacao l = new Locacao();
        l.setCliente(cliente);
        l.setVeiculo(v);
        l.setFilialRetirada(filial);
        l.setRetiradaPrevista(INICIO);
        l.setDevolucaoPrevista(FIM);
        l.adicionarOpcional(gps, 2);
        locacoes.save(l);

        // o valor do opcional muda depois da reserva: a locação mantém o valor antigo
        gps.setValorDia(new BigDecimal("99.00"));
        limparCache();

        List<LocacaoOpcional> itens = locacaoOpcionais.findByLocacaoId(l.getId());
        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).getQuantidade()).isEqualTo(2);
        assertThat(itens.get(0).getValorDia()).isEqualByComparingTo("10.00");
    }

    @Test
    void valorPrevisto_somaDiariasEOpcionais() {
        Opcional cadeirinha = new Opcional();
        cadeirinha.setNome("Cadeirinha");
        cadeirinha.setValorDia(new BigDecimal("10.00"));
        cadeirinha = opcionais.save(cadeirinha);

        Locacao l = new Locacao();
        l.setVeiculo(novoVeiculo("BBB2B22"));
        l.setRetiradaPrevista(INICIO);
        l.setDevolucaoPrevista(FIM);
        l.adicionarOpcional(cadeirinha, 2);

        // 3 diárias × 100 + (10 × 2 × 3) = 360
        assertThat(l.calcularValorPrevisto()).isEqualByComparingTo("360.00");
    }

    @Test
    void conflitoDeAgenda_detectaSobreposicaoMasNaoPeriodosEmSequencia() {
        Veiculo v = novoVeiculo("CCC3C33");
        novaLocacao(v, INICIO, FIM);

        assertThat(locacoes.existeConflito(v.getId(), INICIO.plusDays(1), FIM.plusDays(1))).isTrue();
        // começa exatamente quando a outra termina: não há conflito
        assertThat(locacoes.existeConflito(v.getId(), FIM, FIM.plusDays(2))).isFalse();
        // antes do período
        assertThat(locacoes.existeConflito(v.getId(), INICIO.minusDays(3), INICIO)).isFalse();
    }

    @Test
    void locacaoCancelada_naoBloqueiaOVeiculo() {
        Veiculo v = novoVeiculo("DDD4D44");
        Locacao l = novaLocacao(v, INICIO, FIM);
        l.cancelar("Cliente desistiu");
        limparCache();

        assertThat(locacoes.existeConflito(v.getId(), INICIO, FIM)).isFalse();
        assertThat(locacoes.findById(l.getId()).orElseThrow().getStatus()).isEqualTo(StatusLocacao.CANCELADA);
    }

    @Test
    void manutencao_detectaSobreposicao() {
        Veiculo v = novoVeiculo("EEE5E55");
        Manutencao m = new Manutencao();
        m.setVeiculo(v);
        m.setTipo("Revisão");
        m.setDataInicio(LocalDate.of(2026, 12, 2));
        m.setDataFim(LocalDate.of(2026, 12, 3));
        m.setCusto(new BigDecimal("300.00"));
        manutencoes.save(m);

        assertThat(manutencoes.existeSobreposicao(v.getId(), LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 5))).isTrue();
        assertThat(manutencoes.existeSobreposicao(v.getId(), LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 6))).isFalse();
    }

    @Test
    void veiculosDisponiveis_excluiLocadosEEmManutencao() {
        Veiculo livre = novoVeiculo("FFF6F66");
        Veiculo locado = novoVeiculo("GGG7G77");
        Veiculo emManutencao = novoVeiculo("HHH8H88");
        Veiculo inativo = novoVeiculo("III9I99");
        inativo.setAtivo(false);

        novaLocacao(locado, INICIO.minusDays(1), FIM.plusDays(1));

        Manutencao m = new Manutencao();
        m.setVeiculo(emManutencao);
        m.setTipo("Troca de óleo");
        m.setDataInicio(LocalDate.of(2026, 12, 2));
        m.setDataFim(LocalDate.of(2026, 12, 3));
        m.setCusto(new BigDecimal("150.00"));
        manutencoes.save(m);
        limparCache();

        Page<Veiculo> pagina = veiculos.buscarDisponiveis(INICIO, FIM, null, null, null, null, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).extracting(Veiculo::getPlaca).containsExactly("FFF6F66");
        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(livre.getId()).isNotNull();
    }

    @Test
    void veiculosDisponiveis_aplicaFiltrosDePreco() {
        novoVeiculo("JJJ1J11");

        Page<Veiculo> caro = veiculos.buscarDisponiveis(INICIO, FIM, null, null,
                new BigDecimal("150.00"), null, PageRequest.of(0, 10));
        Page<Veiculo> naFaixa = veiculos.buscarDisponiveis(INICIO, FIM, filial.getId(), categoria.getId(),
                new BigDecimal("50.00"), new BigDecimal("150.00"), PageRequest.of(0, 10));

        assertThat(caro.getContent()).isEmpty();
        assertThat(naFaixa.getContent()).hasSize(1);
    }

    @Test
    void fluxoCompleto_reservaRetiradaDevolucaoComMulta() {
        Veiculo v = novoVeiculo("KKK2K22");
        Locacao l = novaLocacao(v, INICIO, FIM);
        assertThat(l.getValorTotal()).isEqualByComparingTo("300.00");

        l.registrarRetirada(1000, INICIO);
        assertThat(l.getStatus()).isEqualTo(StatusLocacao.ATIVA);
        assertThat(v.getStatus()).isEqualTo(StatusVeiculo.ALUGADO);

        // devolve com 2 dias de atraso: 300 + 2 × 50 = 400
        l.registrarDevolucao(1500, FIM.plusDays(2));
        limparCache();

        Locacao resultado = locacoes.findDetalhadaById(l.getId()).orElseThrow();
        assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.FINALIZADA);
        assertThat(resultado.getKmInicial()).isEqualTo(1000);
        assertThat(resultado.getKmFinal()).isEqualTo(1500);
        assertThat(resultado.getValorTotal()).isEqualByComparingTo("400.00");
        assertThat(resultado.getVeiculo().getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
        assertThat(resultado.getVeiculo().getQuilometragem()).isEqualTo(1500);
    }

    @Test
    void devolucaoNoPrazo_naoGeraMulta() {
        Veiculo v = novoVeiculo("LLL3L33");
        Locacao l = novaLocacao(v, INICIO, FIM);
        l.registrarRetirada(1000, INICIO);
        l.registrarDevolucao(1200, FIM);

        assertThat(l.getValorTotal()).isEqualByComparingTo("300.00");
    }

    @Test
    void saldoDevedor_consideraPagamentosRegistrados() {
        Veiculo v = novoVeiculo("MMM4M44");
        Locacao l = novaLocacao(v, INICIO, FIM);   // 300,00

        pagamentos.save(novoPagamento(l, "100.00", TipoPagamento.SINAL, FormaPagamento.PIX));
        pagamentos.save(novoPagamento(l, "50.00", TipoPagamento.SALDO, FormaPagamento.DINHEIRO));
        limparCache();

        assertThat(pagamentos.somarPorLocacao(l.getId())).isEqualByComparingTo("150.00");
        assertThat(pagamentos.findByLocacaoIdOrderByDataPagamentoAsc(l.getId())).hasSize(2);
        assertThat(locacoes.findById(l.getId()).orElseThrow().calcularSaldoDevedor()).isEqualByComparingTo("150.00");
    }

    @Test
    void somaDePagamentos_semPagamentosRetornaZero() {
        Locacao l = novaLocacao(novoVeiculo("NNN5N55"), INICIO, FIM);
        assertThat(pagamentos.somarPorLocacao(l.getId())).isEqualByComparingTo("0");
    }

    Pagamento novoPagamento(Locacao l, String valor, TipoPagamento tipo, FormaPagamento forma) {
        Pagamento p = new Pagamento();
        p.setLocacao(l);
        p.setValor(new BigDecimal(valor));
        p.setTipo(tipo);
        p.setForma(forma);
        return p;
    }
}
