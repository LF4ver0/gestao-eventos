package org.lfavero.application.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.lfavero.application.dto.response.ErroResponse;
import org.lfavero.application.dto.response.ErroResponse.CampoErro;

import java.util.ArrayList;
import java.util.List;

public class ErrorMappers {

    private static Response resposta(ErroResponse corpo) {
        return Response.status(corpo.status())
                .type(MediaType.APPLICATION_JSON)
                .entity(corpo)
                .build();
    }

    @Provider
    public static class ApiExceptionMapper implements ExceptionMapper<ApiException> {
        @Override
        public Response toResponse(ApiException e) {
            if (e.getCampo() != null) {
                return resposta(new ErroResponse(e.getStatus(), "Dados inválidos",
                        List.of(new CampoErro(e.getCampo(), e.getMessage()))));
            }
            return resposta(ErroResponse.simples(e.getStatus(), e.getMessage()));
        }
    }

    @Provider
    public static class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException e) {
            List<CampoErro> erros = new ArrayList<>();
            for (ConstraintViolation<?> v : e.getConstraintViolations()) {
                erros.add(new CampoErro(nomeDoCampo(v.getPropertyPath()), v.getMessage()));
            }
            return resposta(new ErroResponse(400, "Dados inválidos", erros));
        }

        private static String nomeDoCampo(Path path) {
            String ultimo = "";
            for (Path.Node node : path) {
                ultimo = node.getName();
            }

            return "periodoValido".equals(ultimo) ? "dataFinal" : ultimo;
        }
    }

    @Provider
    public static class JsonMapper implements ExceptionMapper<JsonProcessingException> {
        @Override
        public Response toResponse(JsonProcessingException e) {
            return resposta(ErroResponse.simples(400,
                    "Corpo da requisição inválido. Verifique os tipos dos campos "
                            + "(datas devem estar no formato yyyy-MM-dd)."));
        }
    }
}
