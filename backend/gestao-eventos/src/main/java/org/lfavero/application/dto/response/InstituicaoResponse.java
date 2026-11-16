package org.lfavero.application.dto.response;

import org.lfavero.domain.entities.Instituicao;
import org.lfavero.domain.enums.TipoInstituicao;

public record InstituicaoResponse(
        Integer id,
        String nome,
        TipoInstituicao tipo,
        String tipoDescricao) {

    public static InstituicaoResponse de(Instituicao i) {
        return new InstituicaoResponse(
                i.getId(),
                i.getNome(),
                i.getTipo(),
                i.getTipo().getDescricao()
        );
    }
}