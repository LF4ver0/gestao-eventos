package org.lfavero.application.dto.response;

import java.util.List;

public record ErroResponse(int status, String mensagem, List<CampoErro> erros) {

    public record CampoErro(String campo, String mensagem) {
    }

    public static ErroResponse simples(int status, String mensagem) {
        return new ErroResponse(status, mensagem, List.of());
    }
}