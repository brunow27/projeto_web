package br.edu.catolicasc.locafacil.entity;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Veículo da frota (RF06). Exclusão lógica pelo campo ativo (RNF12). */
@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 8)
    @Column(nullable = false, unique = true, length = 8)
    private String placa;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String marca;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false)
    private int ano;

    @Min(0)
    @Column(nullable = false)
    private int quilometragem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filial_id", nullable = false)
    private Filial filial;

    @OneToMany(mappedBy = "veiculo")
    private List<Locacao> locacoes = new ArrayList<>();

    @OneToMany(mappedBy = "veiculo")
    private List<Manutencao> manutencoes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(int quilometragem) {
        this.quilometragem = quilometragem;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public void setStatus(StatusVeiculo status) {
        this.status = status;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }

    public List<Locacao> getLocacoes() {
        return locacoes;
    }

    public List<Manutencao> getManutencoes() {
        return manutencoes;
    }

    /**
     * Verifica, em memória, se o veículo está livre no período (RN01).
     * Atenção: percorre as coleções lazy; para listagens use a consulta
     * VeiculoRepository.buscarDisponiveis (evita N+1).
     */
    public boolean estaDisponivel(LocalDateTime inicio, LocalDateTime fim) {
        if (!ativo) {
            return false;
        }
        boolean locado = locacoes.stream().anyMatch(l -> l.ocupaPeriodo(inicio, fim));
        boolean emManutencao = manutencoes.stream()
                .anyMatch(m -> m.sobrepoe(inicio.toLocalDate(), fim.toLocalDate()));
        return !locado && !emManutencao;
    }
}
