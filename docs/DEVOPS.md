# DevOps — Docker e Azure

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

## Evolução e limites da prova de conceito

| Frente | Situação |
| --- | --- |
| Java Advanced | CRUD educativo e requisitos técnicos implementados; avaliação acadêmica cabe ao professor |
| Docker | Build e execução validados localmente e demonstrados na VM Azure em 30/10 |
| Produto educativo | Pistas, perguntas, alternativas, conclusão do jogador, feedback e progresso são futuros |
| Mobile e Banco de Dados | Flutter e modelagem Oracle são entregas separadas; integração ainda não demonstrada aqui |
| Persistência durável | Evolução futura; atualmente H2 em memória |
| Segurança, IA e gamificação complexa | Não implementadas nesta Sprint |

A validação Docker de 22/10/2026 teve 30 testes JUnit no build, 33 verificações no primeiro contêiner e 36 em novo contêiner. Consulte o [histórico e as distinções de evidência](EXECUCAO_E_TESTES.md#testes-e-evidências-históricas); não somar essas contagens como uma suíte homogênea. A prova local não substitui implantação e demonstração na VM Azure.

## Prova de conceito Azure — 30/10/2026

A implantação demonstra Docker e Cloud Computing em uma prova de conceito acadêmica; não é uma aplicação de produção. Docker empacota a API e suas dependências, e a VM Azure permite executá-la e demonstrar acesso externo. O H2 permanece integrado à JVM, sem banco Azure separado.

A implantação foi realizada em 30/10/2026, no commit `d7fefaa`. As capturas registram o provisionamento, a configuração de rede e a execução da API. As [evidências selecionadas](evidencias/devops/README.md) estão disponíveis no repositório; os originais com dados privados foram mantidos fora dele. Os recursos Azure foram excluídos após a demonstração. A captura final da consulta confirmando a ausência do grupo ainda não está disponível.

### Arquitetura de implantação

Cliente HTTP → Internet → IP público Azure → controles NSG da sub-rede → NIC/VM Ubuntu → Docker (8080:8080) → Nuclear Case API Spring Boot → H2 em memória.

A VNet contém a sub-rede e a interface da VM. O NSG está **associado à sub-rede**. O IP público está associado à NIC. O NSG representa filtragem de tráfego, não um proxy intermediário. O H2 executa dentro da aplicação e não utiliza Azure SQL.

[Diagrama em PNG e draw.io](arquitetura/README.md), com os recursos identificados e ícones da biblioteca Azure do draw.io.

### Tecnologias e recursos

Azure CLI (provisionamento), Azure Virtual Machines (computação), Azure Virtual Network (rede), Azure Network Security Group (filtragem), Azure Public IP (acesso externo), Ubuntu Server 22.04 LTS, Docker Engine, Java 21, Spring Boot, H2 Database e GitHub.

| Item | Configuração utilizada |
| --- | --- |
| Assinatura | Azure for Students; identificador privado não incluído |
| Região | `spaincentral` |
| Grupo | `rg-nuclear-case` |
| VNet / sub-rede | `vnet-nuclear-case` / `snet-nuclear-case` |
| NSG | `nsg-nuclear-case`, associado à sub-rede |
| IP público / NIC | `pip-nuclear-case` / `nic-nuclear-case` |
| VM / SKU | `vm-nuclear-case` / `Standard_D2s_v4` |
| SO / usuário | Ubuntu Server 22.04 LTS / `azureuser` |
| SSH | Chave pública; TCP 22 restrita ao IP autorizado |
| HTTP | TCP 8080 temporariamente pública |
| Imagem / contêiner | `nuclear-case-api:azure` / `nuclear-case` |

### Provisionamento: histórico e reconstrução

A VM foi criada pela Azure CLI, com o NSG associado à sub-rede. As capturas confirmam a imagem `Ubuntu2204`, disco `Standard_LRS`, NIC existente, sub-rede `10.0.1.0/24` e regras SSH/HTTP com prioridades 100/110. A versão exata da imagem do sistema e o prefixo completo da VNet não foram registrados.

Comando de criação registrado na captura de 30/10. A chave pública foi passada por variável de ambiente:

```bash
az vm create \
  --resource-group rg-nuclear-case \
  --name vm-nuclear-case \
  --location spaincentral \
  --size Standard_D2s_v4 \
  --image Ubuntu2204 \
  --admin-username azureuser \
  --authentication-type ssh \
  --ssh-key-values "$SSH_PUBLIC_KEY" \
  --nics nic-nuclear-case \
  --storage-sku Standard_LRS \
  --output json
```

O [script Bash](../scripts/azure-deploy.sh) contém a sequência equivalente reconstruída: grupo → VNet/sub-rede → NSG/regras → associação à sub-rede → IP → NIC → VM. Foi preparado após a implantação e **não foi executado**. Os parâmetros reconstruídos estão comentados; disponibilidade, permissões e quota devem ser conferidas antes de uma reprodução.

A execução futura exige Bash, Azure CLI já autenticada, Python 3 e OpenSSH (`ssh-keygen`, `head` e `wc` do ambiente Linux), assinatura explícita, chave **pública** `.pub` e IPv4 público autorizado `/32`. Nomes, região, SKU, imagem e CIDRs são configuráveis por variáveis de ambiente documentadas no cabeçalho. O script exige `--apply`, terminal interativo e a frase `PROVISIONAR <grupo>`. Sem isso, não provisiona. Não contém credenciais, login, SSH remoto ou limpeza automática.

Grupo existente é verificado: somente vazio, com tag `project=nuclear-case` e região correspondente pode continuar. Grupos com recursos são recusados para evitar alterações em uma implantação existente. O script não retoma automaticamente execuções interrompidas. Em falha parcial, o script para e os recursos criados podem continuar gerando custo; revisão manual e autorização própria de limpeza são necessárias.

Para reproduzir, definir `AZURE_SUBSCRIPTION`, `SSH_PUBLIC_KEY_FILE` e `SSH_SOURCE_CIDR` no terminal e revisar as demais opções do script. Executar `bash scripts/azure-deploy.sh --apply` somente após confirmar o escopo e os custos. Manter configurações de conta e chaves fora do repositório.

### Docker na VM

O Docker Engine foi validado com `sudo docker run --rm hello-world`. O repositório foi clonado no commit `d7fefaa`; a imagem `nuclear-case-api:azure` foi construída e listada com `docker images`. O contêiner `nuclear-case` foi verificado com `docker ps`, com a porta `8080:8080` publicada. Os comandos exatos de instalação e a linha completa do `docker run` da aplicação não foram registrados.

**Procedimento de instalação para reprodução:** VM Ubuntu 22.04 x86_64 nova, sem Docker ou pacotes conflitantes. A sequência abaixo não é uma transcrição da instalação original e não foi executada nesta revisão. Se já houver Docker ou pacotes conflitantes, revisar a instalação antes de continuar. Conferir o fingerprint SSH por canal confiável antes de conectar.

```bash
# Na VM Ubuntu 22.04 x86_64 nova:
sudo apt-get update
sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
sudo tee /etc/apt/sources.list.d/docker.sources >/dev/null <<'DOCKER_APT'
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: jammy
Components: stable
Architectures: amd64
Signed-By: /etc/apt/keyrings/docker.asc
DOCKER_APT
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo docker run --rm hello-world
```

Os pacotes atuais são selecionados pelo repositório; a versão histórica instalada não foi registrada. Essa sequência é específica da VM x86_64 Ubuntu Jammy usada como referência.

**Procedimento de implantação para reprodução:** após instalar Docker, usar um diretório novo e conferir que a porta 8080 e o nome do contêiner estão livres. Os comandos abaixo combinam as etapas registradas com a linha de execução equivalente:

```bash
git clone https://github.com/gi13090/nuclear-case-api.git nuclear-case-api
cd nuclear-case-api
git checkout --detach d7fefaa
git rev-parse HEAD
sudo docker build -t nuclear-case-api:azure .
sudo docker images nuclear-case-api
sudo docker run -d --name nuclear-case -p 8080:8080 nuclear-case-api:azure
sudo docker ps --filter name=nuclear-case
```

A porta 8080 do host é encaminhada à 8080 do contêiner. O Dockerfile multi-stage Maven/Java 21 é o mesmo já validado localmente, com runtime sem privilégios. Nenhuma alteração nele foi necessária. A instalação e o deploy são manuais e documentados; o script `.sh` provisiona apenas infraestrutura.

**Verificações HTTP para reprodução**, a partir de um computador externo. Não foram repetidas nesta revisão:

```bash
curl --fail-with-body "http://${PUBLIC_IP:?Defina o IP no terminal}:8080/api/casos"
curl --fail-with-body -L "http://${PUBLIC_IP:?Defina o IP no terminal}:8080/swagger-ui.html"
```

### Resultados e limites da validação

| Verificação em 30/10 | Resultado | Evidência |
| --- | --- | --- |
| Docker Engine / hello-world | Sucesso | [hello-world](evidencias/devops/03-docker-hello-world.jpeg) |
| Build Docker | Concluído | [Build concluído](evidencias/devops/04-docker-build.jpeg) |
| Imagem / docker ps | Imagem existente e contêiner em execução | [Imagem](evidencias/devops/05-docker-images.jpeg); captura de docker ps preservada fora do Git por conter dados privados |
| GET público `/api/casos` | Três casos iniciais | [GET público](evidencias/devops/07-api-publica.jpeg) |
| Swagger público | Seis endpoints exibidos | [Swagger](evidencias/devops/08-swagger.jpeg) |

Os casos retornados foram “O alimento irradiado”, “O segredo do diagnóstico” e “O futuro energético”. Visualizar seis operações no Swagger **não comprova execução funcional de todas elas**. As evidências disponíveis não comprovam a validação do CRUD completo na Azure. Os testes locais históricos estão separados no início deste documento e em [Execução e testes](EXECUCAO_E_TESTES.md#testes-e-evidências-históricas).

### Segurança e custos

O acesso SSH utilizou chave pública, com TCP 22 restrita ao IP autorizado. TCP 8080 esteve aberta temporariamente à internet para a demonstração. A API não possui autenticação nem HTTPS nesta prova de conceito. O banco H2 é volátil e integrado à aplicação; encerrar a JVM perde as alterações. Não há persistência Azure externa. Revisar e ocultar credenciais, identificadores de conta e dados pessoais antes de publicar capturas, preservando originais privados.

A VM utilizada foi **Standard_D2s_v4**. Na calculadora consultada em **30/10/2026**, quatro horas de computação foram estimadas em aproximadamente **US$ 0,43**. Não é cotação atual nem custo total: disco, IP público, tráfego e tempo efetivo podem acrescentar valores. A estimativa registrada considera Spain Central e D2s v4. Não representa a cobrança efetiva da assinatura.

### Exclusão dos recursos

Os recursos Azure do projeto foram excluídos após a demonstração. O vídeo registra `az group delete` para `rg-nuclear-case` e o retorno ao prompt. **A captura final da consulta `az group exists` retornando `false` ainda não está disponível.** A pendência é o registro dessa confirmação, sem necessidade de repetir a exclusão.

O procedimento abaixo serve de referência para encerrar futuras reproduções. Conferir a assinatura e o conteúdo do grupo antes de excluir; o grupo deve ser dedicado ao projeto. Esses comandos não foram executados durante esta revisão documental.

```bash
# Definir AZURE_SUBSCRIPTION no terminal privado e conferir escopo:
az group show --subscription "${AZURE_SUBSCRIPTION:?}" -n rg-nuclear-case --query '{name:name,location:location}' -o table
az resource list --subscription "${AZURE_SUBSCRIPTION:?}" -g rg-nuclear-case --query '[].{name:name,type:type}' -o table
# Somente com autorização específica e gravação; manter confirmação interativa:
az group delete --subscription "${AZURE_SUBSCRIPTION:?}" --name rg-nuclear-case
# Após o comando terminar com sucesso:
az group exists --subscription "${AZURE_SUBSCRIPTION:?}" --name rg-nuclear-case -o tsv
```

Para documentar a confirmação, registrar a resposta `false` de uma consulta concluída com sucesso. Erros de autenticação, permissão ou timeout não comprovam ausência do grupo. O script de provisionamento não executa limpeza automática.

### Referências

[Vídeo demonstrativo](https://youtu.be/xHzolGpopnc) · [Arquitetura](arquitetura/README.md) · [Evidências](evidencias/devops/README.md).

Referências técnicas consultadas em 31/10/2026 para preparar a sequência reproduzível: [Azure CLI — VM](https://learn.microsoft.com/en-us/cli/azure/vm?view=azure-cli-latest), [NIC](https://learn.microsoft.com/en-us/cli/azure/network/nic?view=azure-cli-latest), [sub-rede](https://learn.microsoft.com/en-us/cli/azure/network/vnet/subnet?view=azure-cli-latest), [regras NSG](https://learn.microsoft.com/en-us/cli/azure/network/nsg/rule?view=azure-cli-latest), [grupo de recursos](https://learn.microsoft.com/en-us/cli/azure/group?view=azure-cli-latest) e [Docker Engine no Ubuntu](https://docs.docker.com/engine/install/ubuntu/).
