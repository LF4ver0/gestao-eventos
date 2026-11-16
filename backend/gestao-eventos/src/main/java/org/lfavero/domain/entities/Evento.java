package org.lfavero.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "evento", indexes = {
        @Index(name = "idx_evento_ativo", columnList = "ativo"),
        @Index(name = "idx_evento_instituicao", columnList = "instituicao_id")
})
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "data_inicial", nullable = false)
    private LocalDate dataInicial;

    @Column(name = "data_final", nullable = false)
    private LocalDate dataFinal;

    @Column(nullable = false)
    private Boolean ativo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    public void atualizarDados(
            String nome,
            LocalDate dataInicial,
            LocalDate dataFinal,
            Instituicao instituicao,
            boolean ativo) {

        this.nome = nome.trim();
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.instituicao = instituicao;
        this.ativo = ativo;
    }
}
