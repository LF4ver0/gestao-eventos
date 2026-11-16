package org.lfavero.application.dto.response;

import org.lfavero.domain.entities.Evento;

import java.time.LocalDate;

public record EventoResponse(
        Integer id,
        String nome,
        LocalDate dataInicial,
        LocalDate dataFinal,
        boolean ativo,
        InstituicaoResumo instituicao) {

    public record InstituicaoResumo(Integer id, String nome) {
    }

    public static EventoResponse de(Evento e) {
        return new EventoResponse(
                e.getId(),
                e.getNome(),
                e.getDataInicial(),
                e.getDataFinal(),
                e.getAtivo(),
                new InstituicaoResumo(
                        e.getInstituicao().getId(),
                        e.getInstituicao().getNome()
                )
        );
    }
}