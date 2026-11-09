package br.edu.catolicasc.locafacil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Categoria de veículo (Econômico, SUV, Executivo) — RF04. */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "A diária deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMultaDia;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal caucao;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public BigDecimal getValorMultaDia() {
        return valorMultaDia;
    }

    public void setValorMultaDia(BigDecimal valorMultaDia) {
        this.valorMultaDia = valorMultaDia;
    }

    public BigDecimal getCaucao() {
        return caucao;
    }

    public void setCaucao(BigDecimal caucao) {
        this.caucao = caucao;
    }

    /** Valor das diárias para a quantidade de dias informada (RN03). */
    public BigDecimal calcularDiarias(int dias) {
        return valorDiaria.multiply(BigDecimal.valueOf(dias));
    }
}
