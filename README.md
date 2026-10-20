# Nuclear Case API

API REST educativa de casos investigativos sobre tecnologia nuclear, para o Challenge FIAP/Marinha 2026.

## Estado atual

Base inicial da Sprint 1: aplicação Spring Boot, entidade `CasoInvestigativo`, DTO de entrada validado, H2 e Swagger/OpenAPI. Ainda não existem endpoints de negócio, CRUD, services, repositories de negócio, carga inicial ou tratamento global de exceções. A documentação Swagger pode ser aberta, mas ainda não lista operações de negócio.

## Tecnologias

- Java 21; Maven 3.6.3 ou superior.
- Spring Boot 4.1.0, Spring MVC, Spring Data JPA e Bean Validation.
- H2 em memória, Lombok e Springdoc OpenAPI 3.1.0.
- Sem autenticação, Spring Security, QueryDSL ou integração Oracle.

## Execução

Use um **JDK 21 completo**, com `java` e `javac`, e Maven instalado. Configure `JAVA_HOME` para esse JDK e confira `java -version`, `javac -version` e `mvn -version`.

```bash
mvn clean verify
mvn spring-boot:run
```

Ou execute o JAR gerado:

```bash
java -jar target/nuclear-case-api-0.0.1-SNAPSHOT.jar
```

- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- Porta padrão: 8080; pode ser alterada com a variável `PORT`.
- `/` não possui endpoint; uma resposta 404 nesse caminho é esperada.

O banco `jdbc:h2:mem:nuclearcase` existe somente durante a execução. Hibernate cria e remove o esquema (`create-drop`); não há persistência após encerrar a JVM nem dados de demonstração carregados. O console H2 está desabilitado e sua dependência específica não foi incluída.

### Ambiente auditado

Nesta máquina, o Java padrão é 17. O caminho `/usr/lib/jvm/java-21-openjdk-amd64` contém o runtime 21.0.12, mas não contém `javac`. Existe um JDK Amazon Corretto 25.0.2 completo. A validação local usa esse compilador com `--release 21`, e Maven, testes e aplicação executam no Java 21:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -o -B clean verify \
  -Dmaven.compiler.fork=true \
  -Dmaven.compiler.executable=/home/gio/.sdkman/candidates/java/25.0.2-amzn/bin/javac
```

Esse comando é específico desta máquina. O modo `-o` funciona porque as dependências já estão no cache local. Em outra máquina com JDK 21 completo, use os comandos normais acima; não é necessário instalar Java 25.

## Organização

Pacote-base: `br.com.fiap.tds.tdspa.javaadv.nuclearcase`.

| Pacote | Estado e responsabilidade |
| --- | --- |
| `domainmodel.entities` | Implementado: entidade JPA |
| `resources.dtos` | Implementado: contrato de entrada com validações |
| `infrastructure.configuration` | Implementado: metadados OpenAPI |
| `datasource.repositories` | Planejado: persistência do CRUD |
| `services` | Planejado: interface e implementação do serviço |
| `resources` | Planejado: resource REST |
| `infrastructure.exceptionHandlers` | Planejado: tratamento global de exceções |

Pacotes futuros não recebem classes vazias apenas para materializar a estrutura. A nomenclatura em camadas segue o `blogBackend` do professor. O DTO é um record Java, e a entidade usa Lombok pontualmente, sem `@Data`.

## Contrato de entrada preparado

| Campo | Regra |
| --- | --- |
| `titulo` | Texto obrigatório, até 150 caracteres |
| `descricao` | Texto obrigatório, até 1000 caracteres |
| `categoria` | Texto obrigatório, até 50 caracteres |
| `dificuldade` | Inteiro obrigatório, de 1 a 5 |
| `explicacaoCientifica` | Texto obrigatório, até 2000 caracteres |

O ID é UUID gerado pelo servidor e não pertence ao DTO de entrada. Categoria é texto livre nesta rodada. A validação do DTO está testada diretamente; sua aplicação via HTTP dependerá do futuro resource com `@Valid`.

## Testes

`mvn clean verify` executa testes com servidor em porta aleatória, H2 real e transação de teste revertida ao final:

- Persistência e leitura JPA, UUID gerado e textos nos limites de tamanho.
- Entradas válidas nos extremos de dificuldade e 11 cenários inválidos de Bean Validation.
- Documento OpenAPI, redirecionamento do Swagger, HTML da interface e console H2 indisponível.

Não há testes de CRUD porque ele não foi implementado. Os testes não carregam dados permanentes nem adicionam endpoints auxiliares.

## Próxima etapa

Repository com Query Method textual, service, resource CRUD, respostas HTTP, tratamento global de exceções e dados educativos de teste. Docker e Azure ficam para a etapa de DevOps.

As decisões, evidências de execução, limitações do ambiente e pendências de publicação estão em [PENDENCIAS_E_DECISOES.md](PENDENCIAS_E_DECISOES.md).
