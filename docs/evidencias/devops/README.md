# Índice de evidências DevOps

Atualização de 31/10/2026: o usuário disponibilizou 13 capturas originais da implantação de 30/10, além do `.drawio` e do link do vídeo. As imagens foram inspecionadas. Cinco registros sem identificadores de assinatura ou credenciais visíveis foram selecionados e copiados integralmente para revisão pública, com igualdade SHA-256 verificada; os originais não foram alterados. Não foram copiadas indiscriminadamente todas as capturas.

| Registro | Evidência disponível | Situação / limite |
| --- | --- | --- |
| 1. Criação da VM | Captura original de 10:59:21 e vídeo local | CLI `az vm create`, região, SKU e VM running comprovados; original contém e-mail/ID da assinatura e permanece fora do Git |
| 2. Rede e NSG | Originais de 09:30:42 e 09:33:29 | Regras 22/8080, prioridades 100/110 e associação à sub-rede comprovadas; IP de origem, e-mail e ID da assinatura exigem ocultação antes de publicar cópias |
| 3. Docker instalado | [hello-world](03-docker-hello-world.jpeg) | Mensagem de sucesso do Docker na VM |
| 4. Docker Build | [Build concluído](04-docker-build.jpeg) | Comando correto seguido de FINISHED; inclui tentativa anterior com erro de digitação, sem ocultar o histórico |
| 5. Imagem Docker | [docker images](05-docker-images.jpeg) | Imagem nuclear-case-api:azure e conclusão do build |
| 6. Contêiner funcionando | Original de 13:51:02 | docker ps comprova contêiner em execução e 8080:8080; registro retido fora do Git por mostrar caminho SSH local e IP de acesso do operador |
| 7. API pública | [GET /api/casos](07-api-publica.jpeg) | IP público, porta 8080 e três casos iniciais; não prova demais operações |
| 8. Swagger | [Swagger público](08-swagger.jpeg) | Seis operações exibidas; não comprova execução funcional de todas |
| 9. Exclusão | Vídeo demonstrativo | Recursos excluídos após a demonstração; comando e retorno ao prompt visíveis. Captura final de az group exists=false ainda indisponível |

Capturas complementares comprovam commit `d7fefaa` na VM, Dockerfile, identidade Ubuntu/azureuser e estimativa de US$ 0,43 para quatro horas de D2s v4. Foram revisadas, mas não copiadas por não serem necessárias à seleção principal ou por conterem identificadores desnecessários.

[Vídeo no YouTube](https://youtu.be/xHzolGpopnc): link informado pelo usuário. A ferramenta de consulta não conseguiu abrir o endereço; acesso externo e visibilidade “não listado” permanecem NÃO VERIFICADOS. O vídeo local de referência tem 1min50s, 854×480 e 25 fps; foram incorporadas legendas somente nos trechos presentes (criação, build e exclusão). Arquitetura, docker ps, API e Swagger não aparecem nele; as imagens aqui documentadas não foram automaticamente inseridas no vídeo.

A gravação inclui identificadores de conta/assinatura nas telas da Azure. Revisar esses dados na versão publicada. Os arquivos selecionados aqui ainda devem passar pela revisão final do usuário antes do push. Nenhuma publicação foi realizada nesta sessão.

[Guia](../../DEVOPS.md)
