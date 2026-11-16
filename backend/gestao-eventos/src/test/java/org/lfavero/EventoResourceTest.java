package org.lfavero;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EventoResourceTest {

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    private int criarInstituicao() {
        return given().contentType(ContentType.JSON)
                .body("{\"nome\":\"Instituicao Teste\",\"tipo\":\"SINGULAR\"}")
                .post("/api/instituicoes")
                .then().statusCode(201)
                .extract().path("id");
    }

    private String corpo(LocalDate inicial, LocalDate fim, int instituicaoId) {
        return "{\"nome\":\"Evento Teste\",\"dataInicial\":\"%s\",\"dataFinal\":\"%s\",\"instituicaoId\":%d}"
                .formatted(inicial, fim, instituicaoId);
    }

    @Test
    void eventoQueComecaHojeNasceAtivo() {
        int instituicaoId = criarInstituicao();

        given().contentType(ContentType.JSON)
                .body(corpo(hoje, hoje.plusDays(2), instituicaoId))
                .post("/api/eventos")
                .then().statusCode(201)
                .header("Location", notNullValue())
                .body("ativo", equalTo(true));
    }

    @Test
    void eventoFuturoNasceInativo() {
        int instituicaoId = criarInstituicao();

        given().contentType(ContentType.JSON)
                .body(corpo(hoje.plusDays(5), hoje.plusDays(6), instituicaoId))
                .post("/api/eventos")
                .then().statusCode(201)
                .body("ativo", equalTo(false));
    }

    @Test
    void inicioNoPassadoRetorna400() {
        int instituicaoId = criarInstituicao();

        given().contentType(ContentType.JSON)
                .body(corpo(hoje.minusDays(1), hoje.plusDays(1), instituicaoId))
                .post("/api/eventos")
                .then().statusCode(400)
                .body("erros.campo", hasItem("dataInicial"));
    }

    @Test
    void dataFinalAnteriorAInicialRetorna400() {
        int instituicaoId = criarInstituicao();

        given().contentType(ContentType.JSON)
                .body(corpo(hoje.plusDays(5), hoje.plusDays(1), instituicaoId))
                .post("/api/eventos")
                .then().statusCode(400)
                .body("erros.campo", hasItem("dataFinal"));
    }

    @Test
    void camposObrigatoriosRetornam400() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .post("/api/eventos")
                .then().statusCode(400)
                .body("erros.campo", hasItem("nome"))
                .body("erros.campo", hasItem("dataInicial"))
                .body("erros.campo", hasItem("dataFinal"))
                .body("erros.campo", hasItem("instituicaoId"));
    }

    @Test
    void instituicaoInexistenteRetorna404() {
        given().contentType(ContentType.JSON)
                .body(corpo(hoje, hoje.plusDays(1), 999999))
                .post("/api/eventos")
                .then().statusCode(404);
    }

    @Test
    void sincronizarVigenciaRetorna200() {
        given().contentType(ContentType.JSON)
                .post("/api/eventos/sincronizar-vigencia")
                .then().statusCode(200)
                .body("ativados", notNullValue())
                .body("inativados", notNullValue());
    }
}
