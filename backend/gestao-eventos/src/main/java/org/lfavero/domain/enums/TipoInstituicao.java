package org.lfavero.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoInstituicao {
    CONFEDERACAO("Confederação"),
    SINGULAR("Singular"),
    CENTRAL("Central"),
    COOPERATIVA("Cooperativa");

    private final String descricao;
}
