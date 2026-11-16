package org.lfavero.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.lfavero.domain.enums.TipoInstituicao;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "instituicao")
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoInstituicao tipo;

    @OneToMany(mappedBy = "instituicao")
    private List<Evento> eventos = new ArrayList<>();

    public void atualizarDados(String nome, TipoInstituicao tipo) {
        this.nome = nome.trim();
        this.tipo = tipo;
    }
}
