package org.lfavero;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class InstituicaoResourceTest {

    private int criarInstituicao(String nome) {
        return given().contentType(ContentType.JSON)
                .body("{\"nome\":\"%s\",\"tipo\":\"CENTRAL\"}".formatted(nome))
                .post("/api/instituicoes")
                .then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void criaInstituicao() {
        given().contentType(ContentType.JSON)
                .body("{\"nome\":\"Cooperativa Teste\",\"tipo\":\"SINGULAR\"}")
                .post("/api/instituicoes")
                .then().statusCode(201)
                .body("nome", equalTo("Cooperativa Teste"))
                .body("tipo", equalTo("SINGULAR"));
    }

    @Test
    void nomeVazioRetorna400() {
        given().contentType(ContentType.JSON)
                .body("{\"nome\":\"\",\"tipo\":\"SINGULAR\"}")
                .post("/api/instituicoes")
                .then().statusCode(400)
                .body("erros.campo", hasItem("nome"));
    }

    @Test
    void instituicaoInexistenteRetorna404() {
        given().get("/api/instituicoes/999999").then().statusCode(404);
    }

    @Test
    void excluiInstituicaoSemEventos() {
        int id = criarInstituicao("Sem Eventos");
        given().delete("/api/instituicoes/" + id).then().statusCode(204);
        given().get("/api/instituicoes/" + id).then().statusCode(404);
    }

    @Test
    void naoExcluiInstituicaoComEventos() {
        int id = criarInstituicao("Com Eventos");
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        given().contentType(ContentType.JSON)
                .body("{\"nome\":\"Evento\",\"dataInicial\":\"%s\",\"dataFinal\":\"%s\",\"instituicaoId\":%d}"
                        .formatted(hoje, hoje.plusDays(1), id))
                .post("/api/eventos")
                .then().statusCode(201);

        given().delete("/api/instituicoes/" + id).then().statusCode(409);
    }
}
