# Execução local e testes

[Voltar ao README](../README.md) · [Contrato da API](API.md) · [Docker e DevOps](DEVOPS.md)

## Java e Maven

Requisitos: **JDK 21 completo**, com `java` e `javac`, e Maven 3.6.3 ou superior. Configure `JAVA_HOME` e confira `java -version`, `javac -version` e `mvn -version`.

```bash
mvn clean verify
mvn spring-boot:run
```

Ou execute o JAR após o build:

```bash
java -jar target/nuclear-case-api-0.0.1-SNAPSHOT.jar
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- `/` não possui endpoint; um 404 nessa URL é esperado.

Se 8080 já estiver ocupada, preserve a aplicação existente e escolha outra porta livre. Em Linux/macOS, por exemplo:

```bash
PORT=8081 java -jar target/nuclear-case-api-0.0.1-SNAPSHOT.jar
```

Nesse caso, use 8081 nas URLs. A configuração está no YAML; não é necessário editar o código.

## Exercitar o CRUD

Use a URL base `http://localhost:8080` para a execução padrão Java. Se alterou `PORT` ou usa Docker, ajuste a porta em todos os comandos. Consulte [campos e respostas](API.md#contrato-rest).

```bash
curl -i -X POST http://localhost:8080/api/casos \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Investigação educativa","descricao":"Analise as evidências do caso.","categoria":"ENERGIA","dificuldade":2,"explicacaoCientifica":"Texto educativo em revisão pela equipe."}'

curl http://localhost:8080/api/casos
curl 'http://localhost:8080/api/casos/buscar?titulo=alimento'
```

Copie o `id` retornado e substitua `SEU_UUID` nos comandos seguintes:

```bash
curl http://localhost:8080/api/casos/SEU_UUID

curl -X PUT http://localhost:8080/api/casos/SEU_UUID \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Investigação revisada","descricao":"Analise novas evidências.","categoria":"ENERGIA","dificuldade":3,"explicacaoCientifica":"Texto educativo revisado pela equipe."}'

curl -i -X DELETE http://localhost:8080/api/casos/SEU_UUID
```

## Postman e interpretação de respostas

**No Postman:** selecione o método no campo próprio e informe somente a URL, por exemplo `http://localhost:8080/api/casos`. Para POST/PUT, use corpo raw JSON e `Content-Type: application/json`.

Espere `201` e `Location` no cadastro, `200` nas consultas/atualização, `204` sem corpo na exclusão, `400` para entradas inválidas e `404` para UUID válido inexistente. A busca pode retornar `200` com `[]`. [Exemplos negativos e ProblemDetail](API.md#entradas-inválidas) descrevem a rejeição de dificuldade `2.7` sem alterar registros.

## Testes e evidências históricas

Os testes automatizados estão em [CasoInvestigativoCrudTests](../src/test/java/br/com/fiap/tds/tdspa/javaadv/nuclearcase/CasoInvestigativoCrudTests.java) e [NuclearCaseApplicationTests](../src/test/java/br/com/fiap/tds/tdspa/javaadv/nuclearcase/NuclearCaseApplicationTests.java). Cobrem JPA/UUID, limites dos DTOs, CRUD HTTP, busca, 400/404, documentação, carga sem duplicação e rejeição de dificuldade fracionária. O teste de resposta genérica 500 é direto no handler, sem endpoint artificial de falha.

| Data | Execução histórica | Resultado |
| --- | --- | --- |
| 20/10/2026 | Java: build limpo, auditoria HTTP do JAR com reinício e nova regressão limpa | 30 testes JUnit; 43 verificações HTTP/encerramento; 30 testes JUnit, respectivamente |
| 22/10/2026 | Docker: build da imagem, primeiro contêiner e novo contêiner limpo | 30 testes JUnit no build; 33 verificações HTTP/infraestrutura; 36 verificações de regressão/infraestrutura |
| 25/10/2026 | Postman | Aceite dos testes manuais relatado pelo usuário; não é execução produzida pela auditoria automatizada |

As sequências finais registradas passaram sem falhas. **As contagens têm naturezas diferentes e não devem ser somadas como testes automatizados.** São evidências históricas; a reorganização destes documentos não representa reexecução dessas rodadas. Para uma nova validação, execute os comandos de build/teste acima. Os testes HTTP precisam permitir conexões locais e usam porta aleatória.

A integração Flutter/Oracle não foi demonstrada neste repositório.
