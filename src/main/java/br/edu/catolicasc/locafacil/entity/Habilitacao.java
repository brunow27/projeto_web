package br.edu.catolicasc.locafacil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Habilitação (CNH) do cliente — relação 1:1 com Cliente (RF03). */
@Entity
@Table(name = "habilitacoes")
public class Habilitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "numero_cnh", nullable = false, unique = true, length = 20)
    private String numeroCnh;

    @NotBlank
    @Pattern(regexp = "[A-E]{1,3}", message = "Categoria da CNH inválida")
    @Column(nullable = false, length = 3)
    private String categoria;

    @NotNull
    @Column(nullable = false)
    private LocalDate validade;

    public Long getId() {
        return id;
    }

    public String getNumeroCnh() {
        return numeroCnh;
    }

    public void setNumeroCnh(String numeroCnh) {
        this.numeroCnh = numeroCnh;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    /** CNH vencida na data de hoje. */
    public boolean estaVencida() {
        return estaVencidaEm(LocalDate.now());
    }

    /** CNH vencida em uma data específica (RN02: usar a devolução prevista). */
    public boolean estaVencidaEm(LocalDate data) {
        return validade == null || validade.isBefore(data);
    }
}
