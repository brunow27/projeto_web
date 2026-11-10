package br.edu.catolicasc.locafacil.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Locação de veículo (RF09–RF12, RF15). As transições de status (RN05) ficam nos métodos da entidade. */
@Entity
@Table(name = "locacoes")
public class Locacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "retirada_prevista", nullable = false)
    private LocalDateTime retiradaPrevista;

    @NotNull
    @Column(name = "devolucao_prevista", nullable = false)
    private LocalDateTime devolucaoPrevista;

    @Column(name = "retirada_real")
    private LocalDateTime retiradaReal;

    @Column(name = "devolucao_real")
    private LocalDateTime devolucaoReal;

    @Min(0)
    @Column(name = "km_inicial")
    private Integer kmInicial;

    @Min(0)
    @Column(name = "km_final")
    private Integer kmFinal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLocacao status = StatusLocacao.RESERVADA;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Size(max = 200)
    @Column(name = "motivo_cancelamento", length = 200)
    private String motivoCancelamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filial_retirada_id", nullable = false)
    private Filial filialRetirada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_devolucao_id")
    private Filial filialDevolucao;

    @OneToMany(mappedBy = "locacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocacaoOpcional> opcionais = new ArrayList<>();

    @OneToMany(mappedBy = "locacao")
    private List<Pagamento> pagamentos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public LocalDateTime getRetiradaPrevista() {
        return retiradaPrevista;
    }

    public void setRetiradaPrevista(LocalDateTime retiradaPrevista) {
        this.retiradaPrevista = retiradaPrevista;
    }

    public LocalDateTime getDevolucaoPrevista() {
        return devolucaoPrevista;
    }

    public void setDevolucaoPrevista(LocalDateTime devolucaoPrevista) {
        this.devolucaoPrevista = devolucaoPrevista;
    }

    public LocalDateTime getRetiradaReal() {
        return retiradaReal;
    }

    public void setRetiradaReal(LocalDateTime retiradaReal) {
        this.retiradaReal = retiradaReal;
    }

    public LocalDateTime getDevolucaoReal() {
        return devolucaoReal;
    }

    public void setDevolucaoReal(LocalDateTime devolucaoReal) {
        this.devolucaoReal = devolucaoReal;
    }

    public Integer getKmInicial() {
        return kmInicial;
    }

    public void setKmInicial(Integer kmInicial) {
        this.kmInicial = kmInicial;
    }

    public Integer getKmFinal() {
        return kmFinal;
    }

    public void setKmFinal(Integer kmFinal) {
        this.kmFinal = kmFinal;
    }

    public StatusLocacao getStatus() {
        return status;
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Filial getFilialRetirada() {
        return filialRetirada;
    }

    public void setFilialRetirada(Filial filialRetirada) {
        this.filialRetirada = filialRetirada;
    }

    public Filial getFilialDevolucao() {
        return filialDevolucao;
    }

    public void setFilialDevolucao(Filial filialDevolucao) {
        this.filialDevolucao = filialDevolucao;
    }

    public List<LocacaoOpcional> getOpcionais() {
        return opcionais;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    /** Devolução prevista deve ser posterior à retirada prevista. */
    @AssertTrue(message = "A devolução prevista deve ser posterior à retirada prevista")
    public boolean isPeriodoValido() {
        return retiradaPrevista == null || devolucaoPrevista == null || devolucaoPrevista.isAfter(retiradaPrevista);
    }

    /** Dias cobrados: períodos de 24h, arredondando para cima, mínimo de 1 diária. */
    private static int calcularDias(LocalDateTime inicio, LocalDateTime fim) {
        long minutos = Duration.between(inicio, fim).toMinutes();
        return (int) Math.max(1, Math.ceil(minutos / 1440.0));
    }

    /** RN03: diárias da categoria × dias + opcionais (valor/dia × quantidade × dias). */
    public BigDecimal calcularValorPrevisto() {
        int dias = calcularDias(retiradaPrevista, devolucaoPrevista);
        BigDecimal total = veiculo.getCategoria().calcularDiarias(dias);
        for (LocacaoOpcional lo : opcionais) {
            total = total.add(lo.calcularSubtotal(dias));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** RN04: dias de atraso × valorMultaDia da categoria (zero se não houver atraso). */
    public BigDecimal calcularMulta(LocalDateTime devolucaoEfetiva) {
        if (!devolucaoEfetiva.isAfter(devolucaoPrevista)) {
            return BigDecimal.ZERO.setScale(2);
        }
        long minutosAtraso = Duration.between(devolucaoPrevista, devolucaoEfetiva).toMinutes();
        int diasAtraso = (int) Math.ceil(minutosAtraso / 1440.0);
        return veiculo.getCategoria().getValorMultaDia()
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** RN06: valor total menos o que já foi pago. */
    public BigDecimal calcularSaldoDevedor() {
        BigDecimal total = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        BigDecimal pago = pagamentos.stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.subtract(pago);
    }

    /** Adiciona um opcional congelando o valor/dia vigente no momento da reserva. */
    public LocacaoOpcional adicionarOpcional(Opcional opcional, int quantidade) {
        LocacaoOpcional item = new LocacaoOpcional();
        item.setLocacao(this);
        item.setOpcional(opcional);
        item.setQuantidade(quantidade);
        item.setValorDia(opcional.getValorDia());
        opcionais.add(item);
        return item;
    }

    /** True se a locação bloqueia o veículo no período (RN01). */
    public boolean ocupaPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return (status == StatusLocacao.RESERVADA || status == StatusLocacao.ATIVA)
                && retiradaPrevista.isBefore(fim)
                && devolucaoPrevista.isAfter(inicio);
    }

    /** RF10: RESERVADA → ATIVA. */
    public void registrarRetirada(int km) {
        registrarRetirada(km, LocalDateTime.now());
    }

    public void registrarRetirada(int km, LocalDateTime quando) {
        if (status != StatusLocacao.RESERVADA) {
            throw new IllegalStateException("Só é possível retirar uma locação RESERVADA (status atual: " + status + ")");
        }
        this.kmInicial = km;
        this.retiradaReal = quando;
        this.status = StatusLocacao.ATIVA;
        this.veiculo.setStatus(StatusVeiculo.ALUGADO);
    }

    /** RF11: ATIVA → FINALIZADA, com multa por atraso e valor final. */
    public void registrarDevolucao(int km) {
        registrarDevolucao(km, LocalDateTime.now());
    }

    public void registrarDevolucao(int km, LocalDateTime quando) {
        if (status != StatusLocacao.ATIVA) {
            throw new IllegalStateException("Só é possível devolver uma locação ATIVA (status atual: " + status + ")");
        }
        if (kmInicial != null && km < kmInicial) {
            throw new IllegalArgumentException("A quilometragem final não pode ser menor que a inicial");
        }
        this.kmFinal = km;
        this.devolucaoReal = quando;
        this.valorTotal = calcularValorPrevisto().add(calcularMulta(quando));
        this.status = StatusLocacao.FINALIZADA;
        this.veiculo.setStatus(StatusVeiculo.DISPONIVEL);
        this.veiculo.setQuilometragem(km);
        if (filialDevolucao != null) {
            this.veiculo.setFilial(filialDevolucao);
        }
    }

    /** RF12: RESERVADA → CANCELADA. */
    public void cancelar(String motivo) {
        if (status != StatusLocacao.RESERVADA) {
            throw new IllegalStateException("Só é possível cancelar uma locação RESERVADA (status atual: " + status + ")");
        }
        this.status = StatusLocacao.CANCELADA;
        this.motivoCancelamento = motivo;
    }
}
