package org.lfavero.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.lfavero.domain.enums.TipoInstituicao;

public record InstituicaoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotNull(message = "O tipo é obrigatório")
        TipoInstituicao tipo) {
}