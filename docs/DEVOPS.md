# Docker local e entrega DevOps

[Voltar ao README](../README.md) · [Contrato da API](API.md) · [Execução e testes](EXECUCAO_E_TESTES.md)

## Docker local validado

Com Docker instalado e daemon acessível, confira `docker info`. No Docker Desktop para Linux, `desktop-linux` pode ser o contexto adequado; essa configuração é específica do ambiente, não uma obrigação em toda instalação.

O [Dockerfile](../Dockerfile) usa Maven 3.9.11/JDK 21 no build e JRE 21 no runtime, com bases fixadas por digest. Executa `clean verify` com todos os testes, sem usar o JDK, cache Maven ou `target/` do host. Na validação de 22/10/2026, o build usou Temurin 21.0.9 e o runtime 21.0.12.1. Alterações nos digests exigem nova validação.

```bash
docker build --pull --no-cache --progress=plain -t nuclear-case-api:local .

docker run -d --name nuclear-case-local \
  --memory=512m --cpus=1 \
  -p 127.0.0.1:8081:8080 \
  nuclear-case-api:local
```

Use 8081 somente se estiver livre. Esse mapeamento preserva a porta 8080 do host; a aplicação escuta na porta interna 8080. A [.dockerignore](../.dockerignore) limita o contexto aos arquivos necessários ao build.

Após inicializar: [casos](http://localhost:8081/api/casos), [busca](http://localhost:8081/api/casos/buscar?titulo=alimento), [Swagger](http://localhost:8081/swagger-ui.html) e [OpenAPI](http://localhost:8081/api-docs). Use os [exemplos curl do guia de execução](EXECUCAO_E_TESTES.md#exercitar-o-crud), trocando a porta para 8081.

```bash
docker logs nuclear-case-local
docker exec nuclear-case-local java -version
docker stop --time 30 nuclear-case-local
docker rm nuclear-case-local
```

Pare/remova somente o contêiner que você criou. O runtime usa o usuário sem privilégios `10001:10001`, Java como processo principal e encerramento gracioso por SIGTERM. A imagem local permanece disponível.

Para alterar também a porta interna com `PORT`, ajuste o mapeamento e escolha uma porta host livre:

```bash
docker run -d --name nuclear-case-alt \
  --memory=512m --cpus=1 \
  -e PORT=8082 -p 127.0.0.1:8082:8082 \
  nuclear-case-api:local

docker logs nuclear-case-alt
docker stop --time 30 nuclear-case-alt
docker rm nuclear-case-alt
```

O build precisa de acesso ao registry e ao Maven Central na primeira construção. O H2 continua descartável no contêiner: uma nova JVM restaura os três casos, sem manter alterações da execução anterior. Não há volume de banco nem persistência externa.

## Azure NÃO executada — entrega Cloud pendente

**Azure não foi implantada.** Docker local, isoladamente, não atende à entrega Cloud das páginas 24–28 da V2. O [diagrama do README](../README.md#arquitetura-java) explica o backend e não substitui o futuro desenho de implantação com ícones e serviços reais.

Checklist para uma futura etapa, sem presumir que já tenha sido realizada:

- [ ] Diagrama de implantação com ícones e identificação dos serviços reais de nuvem e sua comunicação.
- [ ] VM provisionada por Azure CLI e contêiner da API funcionando nessa VM.
- [ ] Script `.sh` e comandos Azure CLI utilizados, revisados e documentados.
- [ ] Evidência da exclusão do grupo de recursos após a demonstração.
- [ ] Instruções Cloud e explicação de como Docker/Cloud se aplicam ao Nuclear Case.
- [ ] Vídeo não listado de aproximadamente 3 minutos, mínimo 720p, legível e narrado ou legendado.
- [ ] PDF com nomes, RMs, turma, link do repositório e link do vídeo; links ainda pendentes.

A identificação oficial está na [equipe do README](../README.md#equipe-e-entrega). Não há região, tamanho de VM ou custo definido nesta documentação. Provisionamento e publicação são etapas futuras, não comandos autorizados por este guia.

## Evolução e limites da prova de conceito

| Frente | Situação |
| --- | --- |
| Java Advanced | CRUD educativo e requisitos técnicos implementados; avaliação acadêmica cabe ao professor |
| Docker | Build e execução local validados; comandos disponíveis acima |
| Produto educativo | Pistas, perguntas, alternativas, conclusão do jogador, feedback e progresso são futuros |
| Mobile e Banco de Dados | Flutter e modelagem Oracle são entregas separadas; integração ainda não demonstrada aqui |
| Persistência durável | Evolução futura; atualmente H2 em memória |
| Segurança, IA e gamificação complexa | Não implementadas nesta Sprint |

A validação Docker de 22/10/2026 teve 30 testes JUnit no build, 33 verificações no primeiro contêiner e 36 em novo contêiner. Consulte o [histórico e as distinções de evidência](EXECUCAO_E_TESTES.md#testes-e-evidências-históricas); não somar essas contagens como uma suíte homogênea. A prova local não substitui implantação e demonstração na VM Azure.
