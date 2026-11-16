package org.lfavero.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotNull(message = "A data inicial é obrigatória")
        LocalDate dataInicial,

        @NotNull(message = "A data final é obrigatória")
        LocalDate dataFinal,

        @NotNull(message = "A instituição é obrigatória")
        Integer instituicaoId) {

    @JsonIgnore
    @AssertTrue(message = "A data final deve ser maior ou igual à data inicial")
    public boolean isPeriodoValido() {
        if (dataInicial == null || dataFinal == null) {
            return true;
        }
        return !dataFinal.isBefore(dataInicial);
    }
}
