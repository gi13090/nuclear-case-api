# Nuclear Case API

API REST educativa de casos investigativos sobre tecnologia nuclear, desenvolvida para o Challenge FIAP/Marinha 2026.

## Tecnologias e requisitos

- JDK 21 completo (`java` e `javac`) e Maven 3.6.3 ou superior.
- Spring Boot 4.1.0, Spring MVC, Spring Data JPA e Bean Validation.
- H2 em memória, Lombok e Springdoc OpenAPI 3.1.0.

Configure `JAVA_HOME` para o JDK 21 e confira `java -version`, `javac -version` e `mvn -version` antes do build.

## Executar e testar

```bash
mvn clean verify
mvn spring-boot:run
```

Ou, após o build:

```bash
java -jar target/nuclear-case-api-0.0.1-SNAPSHOT.jar
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- Porta padrão: `8080`, configurável pela variável de ambiente `PORT`.
- O caminho `/` não possui endpoint; retorna 404.

O H2 está em memória, com esquema criado pelo Hibernate (`create-drop`). Os dados de cada execução são perdidos ao encerrar a JVM. O console H2 está desabilitado. A configuração está em `src/main/resources/application.yaml`.

## Funcionalidades

| Método | Endpoint | Resposta de sucesso |
| --- | --- | --- |
| GET | `/api/casos` | 200: lista de casos |
| GET | `/api/casos/{id}` | 200: caso pelo UUID |
| GET | `/api/casos/buscar?titulo=alimento` | 200: busca por trecho do título, sem distinguir maiúsculas e minúsculas |
| POST | `/api/casos` | 201: caso criado e cabeçalho `Location` |
| PUT | `/api/casos/{id}` | 200: atualização completa, preservando o UUID |
| DELETE | `/api/casos/{id}` | 204: exclusão, sem corpo |

Listagem e busca sem resultados retornam `200` com `[]`. O parâmetro `titulo` é obrigatório, não pode estar em branco e aceita até 150 caracteres. Espaços no começo e fim da busca são removidos.

Dados inválidos ou UUID malformado retornam `400`; UUID válido inexistente retorna `404`. Os erros usam `application/problem+json` (`ProblemDetail`); erros de validação incluem o mapa `errors` com mensagens por campo. Falhas inesperadas retornam `500` com mensagem genérica.

## Contrato de entrada

| Campo | Regra |
| --- | --- |
| `titulo` | Texto obrigatório, até 150 caracteres |
| `descricao` | Texto obrigatório, até 1000 caracteres |
| `categoria` | Texto obrigatório, até 50 caracteres |
| `dificuldade` | Inteiro obrigatório, de 1 a 5 |
| `explicacaoCientifica` | Texto obrigatório, até 2000 caracteres |

POST e PUT recebem os mesmos campos. O ID é UUID gerado pelo servidor; não é enviado no corpo de entrada. A resposta contém `id` e todos os campos de conteúdo.

```bash
curl -i -X POST http://localhost:8080/api/casos \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Investigação educativa","descricao":"Analise as evidências do caso.","categoria":"ENERGIA","dificuldade":2,"explicacaoCientifica":"Texto educativo em revisão pela equipe."}'

curl http://localhost:8080/api/casos
curl 'http://localhost:8080/api/casos/buscar?titulo=alimento'
```

Copie o UUID retornado para consultar, atualizar ou excluir:

```bash
curl http://localhost:8080/api/casos/SEU_UUID

curl -X PUT http://localhost:8080/api/casos/SEU_UUID \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Investigação revisada","descricao":"Analise novas evidências.","categoria":"ENERGIA","dificuldade":3,"explicacaoCientifica":"Texto educativo revisado pela equipe."}'

curl -i -X DELETE http://localhost:8080/api/casos/SEU_UUID
```

## Dados de demonstração

Na inicialização de um banco vazio, `DataLoader` insere três casos: **O alimento irradiado**, **O segredo do diagnóstico** e **O futuro energético**. A carga é transacional e só ocorre quando a tabela está vazia; executar o carregador novamente com registros existentes não duplica os dados. Não há `data.sql` nem outra carga concorrente.

Os textos são exemplos educativos conservadores e devem passar por revisão científica humana antes da apresentação final. Referências de apoio à revisão: [irradiação industrial e de alimentos — IAEA](https://nucleus.iaea.org/sites/diif/Pages/GammaRT.aspx), [medicina nuclear — IAEA](https://gnssn.iaea.org/CSN/Toolbox%20Wiki%20temp/Radiation%20Protection%20of%20Patients.aspx) e [avaliação econômica do ciclo de vida nuclear — IAEA](https://nucleus.iaea.org/sites/connect/MSNpublic/Pages/nct/site/bid/evaluate/economic.html).

## Organização

Pacote-base: `br.com.fiap.tds.tdspa.javaadv.nuclearcase`.

| Pacote | Responsabilidade |
| --- | --- |
| `domainmodel.entities` | Entidade JPA `CasoInvestigativo` |
| `datasource.repositories` | Persistência e Query Method textual |
| `services` | Interface e implementação, transações e atualização da entidade carregada |
| `resources` | Resource REST e respostas HTTP |
| `resources.dtos` | Entrada validada e resposta sem exposição direta da entidade |
| `infrastructure.exceptionHandlers` | Tratamento global com ProblemDetail |
| `infrastructure.exceptions` | Exceção de recurso inexistente |
| `infrastructure.configuration` | Metadados OpenAPI |
| `infrastructure.runner` | Dados de demonstração |

## Cobertura dos testes

O comando `mvn clean verify` verifica persistência JPA/H2, geração e preservação de UUID, limites do DTO, ciclo CRUD HTTP, busca textual, erros 400/404, documentação OpenAPI, Swagger UI, console H2 desabilitado, carga sem duplicação e mensagem genérica de erro inesperado.

Os testes HTTP sobem o servidor em porta aleatória; o ambiente precisa permitir conexões locais. Os testes de CRUD usam dados próprios e os removem ao final. O teste do tratamento de falha inesperada é direto no handler; não há endpoint artificial que gere falhas.

Não estão incluídos autenticação, Oracle, PATCH, paginação, filtros combinados, Docker ou Azure.
