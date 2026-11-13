# Testes de contrato com Pact no HoraCerta

## Resumo

O HoraCerta passou a considerar o sindicato ao apurar horas. O cálculo permaneceu correto, mas a mesma entrega trocou chaves e unidades no JSON. A integração perdeu identificação e horas, e o módulo de férias perdeu os dias trabalhados. O laboratório reproduz o incidente e evolui para verificação de compatibilidade por versão.

As regras de sindicato, jornada e frequência são fictícias. `horasTrabalhadas` inclui o período classificado como extra.

## Objetivos

- Exercitar o client real e gerar contratos V3 para 200 e 404.
- Verificar o provider por HTTP, com estado preparado em H2.
- Comparar adição de campo, remoção de chave e mudança de unidade.
- Verificar a matriz do Broker por versão e ambiente antes de implantar.

## Estrutura da pasta

| Material | Uso |
|---|---|
| [Apresentação](apresentacao.md), [HTML](index.html) e [PDF](apresentacao.pdf) | 64 slides com quatro diagramas |
| [Plano](plano-aula.md), [guia](guia-professor.md) e [revisão](revisao.md) | Agenda, perguntas e notas por slide |
| [Exemplos](exemplos/README.md) e [demonstrações](demonstracoes/README.md) | Três projetos independentes e variantes controladas |
| [Exercícios](exercicios/README.md) e [gabaritos](gabarito/README.md) | Três práticas e duas atividades curtas |
| [Roteiro](roteiro-demonstracao.md) e [evidências](evidencias/README.md) | Comandos e resultados de execução |
| [Infraestrutura](infra/README.md), [versões](VERSOES.md) e [fontes](fontes.md) | Broker, CI e dependências |

O pacote de referência está em `testes-de-contrato/` na raiz do repositório.

## Pré-requisitos

Para a primeira prática, use Linux ou WSL, JDK 21, Bash, Python 3, `curl`, `unzip` e `patch`. Maven é baixado pelo wrapper na primeira execução. Git, Docker Engine e Compose são usados na etapa Broker. A alternativa em Docker permite testar e renderizar com os serviços de `compose.yaml`. A primeira execução baixa imagens e dependências.

## Como iniciar o ambiente

Execute os comandos na pasta desta aula. Para a suíte local, o H2 em memória e as portas HTTP dinâmicas são iniciados pelos próprios testes. Para o cenário manual, execute:

```bash
./scripts/iniciar-demo.sh
```

O perfil de build `demo` insere registros fictícios ao iniciar o provider. As portas padrão são 18080, 18081 e 18082. Exporte `APURACAO_PORT`, `INTEGRACAO_PORT` e `FERIAS_PORT` na sessão para alterá-las. A massa de demonstração depende desse perfil.

## Como executar os exemplos

O script de inicialização já calcula F001 e verifica a resposta dos três serviços. Para reproduzir manualmente:

```bash
curl -fsS -X POST http://127.0.0.1:18080/apuracoes/F001/2026-10/calcular
curl -fsS http://127.0.0.1:18080/apuracoes/F001/2026-10
curl -fsS http://127.0.0.1:18081/integracoes/F001/2026-10
curl -fsS http://127.0.0.1:18082/ferias/frequencia/F001/2026-10
curl -sS -i http://127.0.0.1:18080/apuracoes/F404/2026-10
```

F001 entrega 160 h, 8 h extras e 20 dias. F002 tem SIND-LOG e 20 h extras. F003 tem jornada padrão e sindicato nulo. F404 responde 404 até que o POST grave um resumo válido de zero horas e zero dias. Repetir o POST recalcula e atualiza a linha existente. O GET apenas consulta.

## Como executar os testes

Sem Docker ou Broker:

```bash
./scripts/verificar-local.sh
PACTOS_PRONTOS=1 ./scripts/demonstrar-quebra.sh ampla
PACTOS_PRONTOS=1 ./scripts/demonstrar-quebra.sh dias
PACTOS_PRONTOS=1 ./scripts/demonstrar-quebra.sh unidade
```

A sequência equivale a `testar-consumers.sh`, `copiar-pactos.sh` e `testar-provider.sh`. Cada projeto roda `./mvnw -B clean verify`. Os JSONs Pact aparecem em `target/pacts/` e os relatórios em `target/surefire-reports/`. O verificador local só usa os dois pactos conferidos e copiados. Sem `PACTOS_PRONTOS=1`, o comando de regressão gera pactos novamente.

Com Docker:

```bash
export LAB_UID="$(id -u)" LAB_GID="$(id -g)"
docker compose run --build --rm java
docker compose run --rm -e PACTOS_PRONTOS=1 java ./scripts/demonstrar-quebra.sh ampla
```

O script `./scripts/validar.sh` confere Compose, suíte, regressões e apresentação. O [roteiro Broker](infra/README.md) usa `./scripts/verificar-broker.sh` para validar publicação, seleção, webhook, gates e processos HTTP reais em um laboratório isolado.

## Como abrir ou renderizar a apresentação

Abra [index.html](index.html) no navegador ou [apresentacao.pdf](apresentacao.pdf). Mantenha a pasta `assets/` junto ao HTML. As setas avançam e voltam. Pressione **P** para abrir as notas do apresentador.

```bash
./scripts/renderizar-slides.sh
```

O script usa Marp 4.5.1 e Chromium em Docker e verifica links, diagramas, correspondência com a revisão e encaixe dos slides.

## Como executar os exercícios

Leia os [enunciados](exercicios/README.md). `./scripts/preparar-exercicio.sh 01` prepara uma cópia em `local/`. Use 02 ou 03 para as demais práticas.

## Como acessar o gabarito

Os [gabaritos](gabarito/README.md) contêm patches aplicáveis às cópias, explicações e alternativas. Depois de gerar os pactos, execute `./scripts/validar-gabaritos.sh` para rodar as três soluções.

## Como encerrar e limpar o ambiente

```bash
./scripts/parar-demo.sh
docker compose down -v
```

O Compose da aula remove o volume de cache Maven. O script Broker usa um projeto Docker exclusivo e apaga seu volume descartável ao encerrar. Para o Broker iniciado manualmente, `docker compose -f infra/docker-compose.yml down -v` apaga seu histórico local. As cópias em `local/` ficam disponíveis para leitura e podem ser apagadas quando terminarem os exercícios.

## Solução de problemas

| Sintoma | Primeiro passo |
|---|---|
| Wrapper não encontra Java | Rode `java -version` e confira `JAVA_HOME`. |
| Verifier sem pactos | Rode a sequência de geração e cópia. Confira nomes em `target/pacts/`. |
| Variante falha antes da asserção | Abra `local/evidencias/`. O script exige a falha esperada no relatório Surefire. |
| Aplicação não inicia | Verifique a porta e os logs em `local/demo/`. |
| Broker sem saúde ou CLI sem acesso | Confira containers, porta, credenciais e rede do host no [guia](infra/README.md). |
| Gate desconhecido | Verifique se a URL do pacto e o SHA do provider em uso receberam um resultado. |
