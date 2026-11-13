# Pact Broker OSS e integração CI

## Laboratório reproduzível

Na raiz da aula, execute `./scripts/verificar-broker.sh`. O script precisa de JDK 21, Git, Python 3, Bash, Docker e Compose. Cria um Broker isolado com basic auth e PostgreSQL, escolhe portas locais livres, publica a partir de um checkout Git temporário e cria o ambiente didático `aula`. Cada SHA corresponde ao código registrado naquele checkout.

O bootstrap publica os dois pactos, verifica a combinação de três versões e faz, para cada aplicação, gate, inicialização do artefato de build, prontidão HTTP, chamada real e registro. Depois publica a feature de férias, mostra o gate desconhecido e chama o mesmo caminho de verificação do webhook em outro checkout do SHA implantado. Por último, demonstra o gate do provider incompatível. O log e os resultados JSON registram as saídas.

O ambiente `aula` representa o laboratório local. A limpeza encerra os três processos e apaga apenas o volume do projeto Docker criado pela execução. Os logs permanecem em `local/broker.*/evidencias/` ou no caminho absoluto indicado por `EVIDENCIAS_DIR`.

## Broker manual

```bash
cp -n .env.example .env
docker compose -f infra/docker-compose.yml up -d --wait
docker compose -f infra/docker-compose.yml ps
docker compose -f infra/docker-compose.yml logs --tail 100 broker postgres
```

O endereço padrão é `http://127.0.0.1:19292`. O CLI lê `PACT_BROKER_BASE_URL`, `PACT_BROKER_USERNAME` e `PACT_BROKER_PASSWORD` do ambiente. O wrapper usa `pact-broker` instalado ou a imagem fixa do CLI em rede do host. Se o CLI containerizado não alcançar loopback no Docker Desktop, habilite a rede do host ou use o binário oficial no host. [Instalação oficial do CLI](https://docs.pact.io/pact_broker/client_cli/readme).

Para testar só a busca normal após popular um Broker manual, exporte as três variáveis e rode `./scripts/verificar-provider-broker.sh normal`. A publicação fica desativada. Para limpeza manual, `docker compose -f infra/docker-compose.yml down -v` apaga o histórico local daquele Broker.

## Configuração do CI remoto

Os workflows `horacerta-consumer.yml`, `horacerta-provider.yml`, `horacerta-webhook.yml` e `horacerta-laboratorio.yml` ficam em `.github/workflows/` na raiz do repositório. O laboratório roda sem secrets externos. Os três workflows integrados exigem Broker acessível ao runner, variável `PACT_BROKER_BASE_URL` e secrets `PACT_BROKER_USERNAME` e `PACT_BROKER_PASSWORD`.

Cadastre os três pacticipants e a branch principal real antes de publicar. Para ativar o webhook, configure no Broker o evento `contract_requiring_verification_published`, provider `apuracao-horas`, método POST e destino `https://api.github.com/repos/OWNER/REPO/dispatches`. Use [o payload de dispatch](webhook-payload.json), `Accept: application/vnd.github+json`, `Content-Type: application/json` e uma credencial de automação do GitHub com permissão para o dispatch. Configure o header de autorização no armazenamento seguro do Broker. Os valores oficiais são `${pactbroker.consumerName}`, `${pactbroker.pactUrl}`, `${pactbroker.providerVersionNumber}` e `${pactbroker.providerVersionBranch}`. O job valida a origem e o par do pacto antes de consultar e usa o SHA recebido como checkout. [Webhooks do Pact Broker](https://docs.pact.io/pact_broker/webhooks).

Para implantar externamente, forneça um script executável versionado e informe seu caminho relativo à pasta da aula em `HORACERTA_DEPLOY_ADAPTER`. Ele recebe pacticipant, SHA, ambiente e diretório Quarkus nessa ordem. O adaptador é responsável pela implantação desse artefato, pelo rollout e pela saúde. Deve retornar 0 só após confirmar os três. Sem adaptador, o workflow de implantação encerra antes de registrar. O disparo de deploy é manual e exige o ambiente cadastrado.

Provider e consumers compartilham o grupo de concorrência por ambiente e mantêm `cancel-in-progress: false`. GitHub Actions restringe a seção ativa no mesmo repositório. Na fila padrão, uma execução nova pode substituir outra ainda pendente. Ambientes distribuídos entre repositórios exigem coordenação externa ou uma política de coexistência. Rolling update e rollback exigem evidência das combinações que podem coexistir. Pending e WIP ficam desativados. Uma falha pending registrada continua incompatível para o gate consumer. [Concorrência no GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/control-workflow-concurrency).
