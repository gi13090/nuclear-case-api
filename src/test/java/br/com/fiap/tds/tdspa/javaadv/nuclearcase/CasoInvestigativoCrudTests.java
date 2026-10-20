package br.com.fiap.tds.tdspa.javaadv.nuclearcase;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.datasource.repositories.CasoInvestigativoRepository;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.infrastructure.exceptionHandlers.GlobalExceptionHandler;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.infrastructure.runner.DataLoader;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CasoInvestigativoCrudTests {
    @Value("${local.server.port}") private int port;
    @Autowired private CasoInvestigativoRepository repository;
    @Autowired private DataLoader dataLoader;

    private static final String VALID = """
            {"titulo":"Caso HTTP exclusivo","descricao":"Investigação educativa de teste",
             "categoria":"TESTE","dificuldade":2,"explicacaoCientifica":"Texto de teste."}
            """;

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
            builder.header("Content-Type", "application/json");
            builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    void completesCrudPreservingUuidAndPersistingUpdates() throws Exception {
        long initialCount = repository.count();
        var created = request("POST", "/api/casos", VALID);
        assertThat(created.statusCode()).isEqualTo(201);
        String id = JsonPath.read(created.body(), "$.id");
        UUID uuid = UUID.fromString(id);
        try {
            assertThat(created.headers().firstValue("Location")).hasValue("http://localhost:" + port + "/api/casos/" + id);
            assertThat(repository.findById(uuid)).isPresent();
            assertThat(request("GET", "/api/casos/" + id, null).statusCode()).isEqualTo(200);
            var listed = request("GET", "/api/casos", null);
            assertThat(listed.statusCode()).isEqualTo(200);
            List<String> ids = JsonPath.read(listed.body(), "$[*].id");
            assertThat(ids).contains(id);
            var updated = request("PUT", "/api/casos/" + id, VALID.replace("Caso HTTP exclusivo", "Título atualizado"));
            assertThat(updated.statusCode()).isEqualTo(200);
            assertThat((String) JsonPath.read(updated.body(), "$.id")).isEqualTo(id);
            assertThat(repository.findById(uuid).orElseThrow().getTitulo()).isEqualTo("Título atualizado");
            assertThat(repository.count()).isEqualTo(initialCount + 1);
            var deleted = request("DELETE", "/api/casos/" + id, null);
            assertThat(deleted.statusCode()).isEqualTo(204);
            assertThat(deleted.body()).isEmpty();
            assertThat(repository.findById(uuid)).isEmpty();
            assertThat(request("GET", "/api/casos/" + id, null).statusCode()).isEqualTo(404);
            assertThat(repository.count()).isEqualTo(initialCount);
        } finally {
            repository.findById(uuid).ifPresent(repository::delete);
        }
    }

    @Test
    void searchesTitleIgnoringCaseAndReturnsEmptyResults() throws Exception {
        var found = request("GET", "/api/casos/buscar?titulo=ALIMENTO", null);
        assertThat(found.statusCode()).isEqualTo(200);
        List<String> titles = JsonPath.read(found.body(), "$[*].titulo");
        assertThat(titles).contains("O alimento irradiado");
        var empty = request("GET", "/api/casos/buscar?titulo=termo-sem-resultados-xyz", null);
        assertThat(empty.statusCode()).isEqualTo(200);
        assertThat(empty.body()).isEqualTo("[]");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void returnsProblemDetailForMissingId(String method) throws Exception {
        var response = request(method, "/api/casos/" + UUID.randomUUID(), method.equals("PUT") ? VALID : null);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/problem+json");
        assertThat((Integer) JsonPath.read(response.body(), "$.status")).isEqualTo(404);
        assertThat(response.body()).doesNotContain("stackTrace", "java.lang");
    }

    @Test
    void rejectsInvalidCreateAndUpdateWithoutChangingPersistence() throws Exception {
        long initialCount = repository.count();
        String invalid = VALID.replace("Caso HTTP exclusivo", " ").replace("\"dificuldade\":2", "\"dificuldade\":6");
        var response = request("POST", "/api/casos", invalid);
        assertThat(response.statusCode()).isEqualTo(400);
        Map<String, String> errors = JsonPath.read(response.body(), "$.errors");
        assertThat(errors).containsKeys("titulo", "dificuldade");
        assertThat(repository.count()).isEqualTo(initialCount);
        var created = request("POST", "/api/casos", VALID);
        String id = JsonPath.read(created.body(), "$.id");
        UUID uuid = UUID.fromString(id);
        try {
            assertThat(request("PUT", "/api/casos/" + id, invalid).statusCode()).isEqualTo(400);
            assertThat(repository.findById(uuid).orElseThrow().getTitulo()).isEqualTo("Caso HTTP exclusivo");
        } finally {
            repository.deleteById(uuid);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/casos/nao-e-uuid", "/api/casos/buscar", "/api/casos/buscar?titulo=%20"})
    void rejectsInvalidParameters(String path) throws Exception {
        var response = request("GET", path, null);
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat((Integer) JsonPath.read(response.body(), "$.status")).isEqualTo(400);
        assertThat(response.body()).doesNotContain("stackTrace");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{}", "{\"dificuldade\":\"abc\"}"})
    void rejectsMalformedOrIncompleteJson(String json) throws Exception {
        assertThat(request("POST", "/api/casos", json).statusCode()).isEqualTo(400);
    }

    @Test
    void initializesExactlyThreeDemonstrationCasesWithoutDuplicates() throws Exception {
        assertThat(repository.count()).isEqualTo(3);
        dataLoader.run();
        dataLoader.run();
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    void documentsAllBusinessOperationsAndErrors() throws Exception {
        var response = request("GET", "/api-docs", null);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> paths = JsonPath.read(response.body(), "$.paths");
        assertThat(paths).containsKeys("/api/casos", "/api/casos/{id}", "/api/casos/buscar");
        Map<String, Object> operations = JsonPath.read(response.body(), "$.paths['/api/casos/{id}']");
        assertThat(operations).containsKeys("get", "put", "delete");
        Map<String, Object> createResponses = JsonPath.read(response.body(), "$.paths['/api/casos'].post.responses");
        assertThat(createResponses).containsKeys("201", "400", "500");
        Map<String, Object> readResponses = JsonPath.read(response.body(), "$.paths['/api/casos/{id}'].get.responses");
        assertThat(readResponses).containsKeys("200", "400", "404", "500");
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        var problem = new GlobalExceptionHandler().inesperado(new IllegalStateException("segredo-interno-de-teste"));
        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).doesNotContain("segredo-interno-de-teste", "IllegalStateException");
    }
}
