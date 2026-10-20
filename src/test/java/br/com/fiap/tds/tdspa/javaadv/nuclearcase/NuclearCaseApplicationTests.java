package br.com.fiap.tds.tdspa.javaadv.nuclearcase;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.resources.dtos.CasoInvestigativoRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NuclearCaseApplicationTests {
    @Autowired private EntityManager entityManager;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private Validator validator;
    @Autowired private Environment environment;
    @Value("${local.server.port}") private int port;

    @Test
    void persistsAndReadsCaseWithGeneratedUuidAndMaximumTextLengths() {
        transactionTemplate.executeWithoutResult(status -> {
            var caso = new CasoInvestigativo();
            caso.setTitulo("T".repeat(150));
            caso.setDescricao("D".repeat(1000));
            caso.setCategoria("C".repeat(50));
            caso.setDificuldade(5);
            caso.setExplicacaoCientifica("E".repeat(2000));
            entityManager.persist(caso);
            entityManager.flush();
            assertThat(caso.getId()).isNotNull();
            entityManager.clear();
            var encontrado = entityManager.find(CasoInvestigativo.class, caso.getId());
            assertThat(encontrado.getTitulo()).isEqualTo(caso.getTitulo());
            assertThat(encontrado.getDescricao()).isEqualTo(caso.getDescricao());
            assertThat(encontrado.getCategoria()).isEqualTo(caso.getCategoria());
            assertThat(encontrado.getDificuldade()).isEqualTo(5);
            assertThat(encontrado.getExplicacaoCientifica()).isEqualTo(caso.getExplicacaoCientifica());
            status.setRollbackOnly();
        });
    }

    @Test
    void acceptsInputAtBothDifficultyBoundariesAndMaximumTextLengths() {
        for (int dificuldade : new int[]{1, 5}) {
            var dto = new CasoInvestigativoRequestDTO("T".repeat(150), "D".repeat(1000),
                    "C".repeat(50), dificuldade, "E".repeat(2000));
            assertThat(validator.validate(dto)).isEmpty();
        }
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void rejectsInvalidInput(CasoInvestigativoRequestDTO dto, String campo) {
        assertThat(validator.validate(dto))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(campo));
    }

    static Stream<Arguments> invalidInputs() {
        return Stream.of(
                Arguments.of(new CasoInvestigativoRequestDTO(" ", "D", "C", 1, "E"), "titulo"),
                Arguments.of(new CasoInvestigativoRequestDTO("T".repeat(151), "D", "C", 1, "E"), "titulo"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", null, "C", 1, "E"), "descricao"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D".repeat(1001), "C", 1, "E"), "descricao"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", " ", 1, "E"), "categoria"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", "C".repeat(51), 1, "E"), "categoria"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", "C", null, "E"), "dificuldade"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", "C", 0, "E"), "dificuldade"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", "C", 6, "E"), "dificuldade"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", "C", 1, ""), "explicacaoCientifica"),
                Arguments.of(new CasoInvestigativoRequestDTO("T", "D", "C", 1, "E".repeat(2001)), "explicacaoCientifica")
        );
    }

    @Test
    void servesOpenApiAndSwaggerAndKeepsH2ConsoleDisabled() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var docs = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api-docs"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(docs.statusCode()).isEqualTo(200);
            assertThat(docs.body()).contains("\"openapi\"", "Nuclear Case API");
            var redirect = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/swagger-ui.html"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(redirect.statusCode()).isBetween(300, 399);
            var ui = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/swagger-ui/index.html"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(ui.statusCode()).isEqualTo(200);
            assertThat(ui.body()).contains("Swagger UI");
            assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isFalse();
            var console = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/h2-console"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(console.statusCode()).isEqualTo(404);
        }
    }
}
