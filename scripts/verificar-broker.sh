#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/processos-demo.sh"

# Banco, portas, processos e checkout exclusivos desta execução.
mkdir -p "$aula/local"
laboratorio=$(mktemp -d "$aula/local/broker.XXXXXX")
estado="$laboratorio/processos"
export DEPLOY_ENVIRONMENT=aula PACT_PUBLICAR_RESULTADOS=true
export PACT_BROKER_USERNAME=professor PACT_BROKER_PASSWORD=horacerta-demo PACT_DB_PASSWORD=db-horacerta-demo
unset PACT_BROKER_TOKEN PACT_BROKER_CAN_I_DEPLOY_IGNORE PACT_BROKER_CAN_I_DEPLOY_DRY_RUN
mapfile -t livres < <(python3 - <<'PY'
import socket
from contextlib import ExitStack
with ExitStack() as pilha:
    for _ in range(4):
        sock = pilha.enter_context(socket.socket())
        sock.bind(("127.0.0.1", 0))
        print(sock.getsockname()[1])
PY
)
PACT_BROKER_PORT=${PACT_PORTA_LAB:-${livres[0]}}
export PACT_BROKER_PORT PACT_BROKER_BASE_URL="http://127.0.0.1:$PACT_BROKER_PORT"
portas[apuracao-horas]=${livres[1]}
portas[integracao-cliente]=${livres[2]}
portas[modulo-ferias]=${livres[3]}
export PACT_GATE_RETRIES=0
projeto="horacerta-broker-${laboratorio##*.}"
projeto=${projeto,,}
compose=(docker compose -p "$projeto" -f "$aula/infra/docker-compose.yml")
export PERFIL_QUARKUS_BUILD=demo
logs=${EVIDENCIAS_DIR:-"$laboratorio/evidencias"}
mkdir -p "$logs"
logs=$(realpath "$logs")

limpar() {
  local retorno=$? app
  trap - EXIT
  for app in "${apps[@]}"; do parar_app "$app"; done
  printf 'Encerrando o Broker e apagando somente o volume descartável %s_historico-broker.\n' "$projeto"
  "${compose[@]}" down -v
  printf 'Evidências e snapshots desta execução em %s\n' "$laboratorio"
  exit "$retorno"
}
trap limpar EXIT
"${compose[@]}" config --quiet
"${compose[@]}" up -d --wait --wait-timeout 180

snapshot="$laboratorio/checkout"
mkdir -p "$snapshot"
tar -C "$aula" --exclude=target --exclude='*/src/test/resources/pacts/*.json' \
  -cf - .gitignore exemplos scripts demonstracoes | tar -C "$snapshot" -xf -
git -C "$snapshot" init -q -b main
git -C "$snapshot" add .
git -C "$snapshot" -c user.name='HoraCerta Aula' -c user.email='aula@horacerta.invalid' commit -qm 'Linha de base do laboratório'
base=$(git -C "$snapshot" rev-parse HEAD)
printf 'SHA local da linha de base %s\n' "$base"
cd "$snapshot"

cli() { "$aula/scripts/broker-cli.sh" "$@"; }
for app in "${apps[@]}"; do
  cli create-or-update-pacticipant --name "$app" --main-branch main
done
cli create-environment --name aula --display-name 'Aula local' --no-production
for consumer in "${consumers[@]}"; do
  APP_BRANCH=main ./scripts/publicar-consumer-ci.sh "$consumer"
done
APP_BRANCH=main ./scripts/verificar-provider-ci.sh normal
cli can-i-deploy \
  --pacticipant apuracao-horas --version "$base" \
  --pacticipant integracao-cliente --version "$base" \
  --pacticipant modulo-ferias --version "$base"

# Gates, processos e registro são sequenciais e usam o mesmo checkout imutável.
for app in "${apps[@]}"; do
  APP_BRANCH=main ./scripts/empacotar-ci.sh "$app"
  artefato="$snapshot/local/artefatos/$app"
  (cd "$artefato" && sha256sum --check --quiet checksums.txt)
  PACTICIPANT=$app APP_VERSION=$base ./scripts/gate.sh
  iniciar_app "$app" "$artefato/quarkus-app"
  cenario_app "$app"
  cli record-deployment --pacticipant "$app" --version "$base" --environment aula
done

git switch -qc feature/sindicato
patch --batch --fuzz=0 -d exemplos/modulo-ferias -p1 < demonstracoes/feature-sindicato.patch
git add exemplos/modulo-ferias/src
git -c user.name='HoraCerta Aula' -c user.email='aula@horacerta.invalid' commit -qm 'Férias consulta o sindicato'
feature=$(git rev-parse HEAD)
APP_BRANCH=feature/sindicato ./scripts/publicar-consumer-ci.sh modulo-ferias
status=0
PACTICIPANT=modulo-ferias APP_VERSION=$feature ./scripts/gate.sh --output json >"$logs/gate-desconhecido.json" || status=$?
printf 'Gate do consumer antes da verificação: saída %s\n' "$status"
[[ "$status" == 1 ]]
python3 - "$logs/gate-desconhecido.json" <<'PY'
import json, sys
with open(sys.argv[1]) as arquivo:
    matriz = json.load(arquivo)
assert matriz["summary"]["deployable"] is None and matriz["summary"]["unknown"] > 0, matriz
print(matriz["summary"]["reason"])
PY

# Emula a entrega do evento. O verificador recebe o SHA implantado e o pacto exato.
git clone -q --no-local "$snapshot" "$laboratorio/webhook"
(
  cd "$laboratorio/webhook"
  git checkout -q --detach "$base"
  export PROVIDER_SHA=$base APP_BRANCH=main PACT_WEBHOOK_CONSUMER=modulo-ferias
  PACT_WEBHOOK_URL=$(python3 - "$logs/gate-desconhecido.json" "$feature" <<'PYTHON'
import json
import sys
with open(sys.argv[1]) as arquivo:
    matriz = json.load(arquivo)
pactos = [linha["pact"]["_links"]["self"]["href"] for linha in matriz["matrix"]
          if linha["consumer"]["version"]["number"] == sys.argv[2]]
url, = pactos
print(url)
PYTHON
)
  export PACT_WEBHOOK_URL
  ./scripts/verificar-provider-ci.sh webhook
)
PACTICIPANT=modulo-ferias APP_VERSION=$feature ./scripts/gate.sh

# Verifica e registra resultado incompatível sem executar deploy.
git switch -q -c regressao/dias "$base"
patch --batch --fuzz=0 -d exemplos/apuracao-horas -p1 < demonstracoes/dias.patch
git add exemplos/apuracao-horas/src
git -c user.name='HoraCerta Aula' -c user.email='aula@horacerta.invalid' commit -qm 'Regressão de dias'
regressao=$(git rev-parse HEAD)
status=0
maven "$snapshot/exemplos/apuracao-horas" clean test -Ppact-broker -Dtest=ApuracaoBrokerContractTest \
  "-Dpact.provider.version=$regressao" -Dpact.provider.branch=regressao/dias \
  -Dpactbroker.providerBranch=regressao/dias -Dpact.verifier.publishResults=true \
  >"$logs/broker-dias.log" 2>&1 || status=$?
printf 'Verifier do provider incompatível: saída %s\n' "$status"
[[ "$status" == 1 ]]
python3 "$aula/scripts/artefatos.py" relatorio \
  "$snapshot/exemplos/apuracao-horas/target/surefire-reports" ApuracaoBrokerContractTest dias
status=0
PACTICIPANT=apuracao-horas APP_VERSION=$regressao ./scripts/gate.sh --output json >"$logs/gate-incompativel.json" || status=$?
printf 'Gate do provider incompatível: saída %s\n' "$status"
[[ "$status" == 1 ]]
python3 - "$logs/gate-incompativel.json" <<'PY'
import json, sys
with open(sys.argv[1]) as arquivo:
    matriz = json.load(arquivo)
assert matriz["summary"]["deployable"] is False and matriz["summary"]["failed"] > 0, matriz
print(matriz["summary"]["reason"])
PY
echo 'Broker validado com verificação normal, pacto exato no caminho webhook e gates dos dois lados.'
