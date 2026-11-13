#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
provider=${PROVIDER_PROJETO:-"$aula/exemplos/apuracao-horas"}
modo=${1:-normal}
: "${PACT_BROKER_BASE_URL:?Informe a URL do Broker}"
: "${PACT_BROKER_USERNAME:?Informe o usuário basic auth}"
: "${PACT_BROKER_PASSWORD:?Informe a senha basic auth}"
for nome in PACT_BROKER_BASE_URL PACT_BROKER_USERNAME PACT_BROKER_PASSWORD; do
  [[ ${!nome} != *'${'* ]] || { echo "Valor não resolvido em $nome" >&2; exit 2; }
done

perfil=pact-broker
classe=ApuracaoBrokerContractTest
criterio=contar
if [[ $modo == webhook ]]; then
  perfil=pact-webhook
  classe=ApuracaoWebhookContractTest
  criterio=webhook
  : "${PACT_WEBHOOK_URL:?Informe a URL exata do pacto}"
  : "${PACT_WEBHOOK_CONSUMER:?Informe o consumer do evento}"
  python3 "$aula/scripts/validar-pacto-webhook.py"
elif [[ $modo != normal ]]; then
  echo 'Uso: verificar-provider-broker.sh normal|webhook' >&2
  exit 2
fi
publish=${PACT_PUBLICAR_RESULTADOS:-false}
case "$publish" in
  true)
    : "${APP_VERSION:?Informe a versão do código executado}"
    : "${APP_BRANCH:?Informe a branch do código executado}"
    [[ $APP_VERSION =~ ^([0-9a-f]{40}|[0-9a-f]{64}|local-[0-9a-f]{64})$ ]] || {
      echo 'Informe SHA de checkout ou hash de snapshot local' >&2; exit 2;
    }
    git check-ref-format --branch "$APP_BRANCH" >/dev/null
    ;;
  false) ;;
  *) echo 'PACT_PUBLICAR_RESULTADOS aceita true ou false' >&2; exit 2 ;;
esac

printf 'Verificador %s. Provider %s, branch %s, publicação %s.\n' "$classe" "${APP_VERSION:-local}" "${APP_BRANCH:-main}" "$publish"
maven "$provider" clean verify "-P$perfil" \
  "-Dpact.provider.version=${APP_VERSION:-local}" \
  "-Dpact.provider.branch=${APP_BRANCH:-main}" \
  "-Dpactbroker.providerBranch=${APP_BRANCH:-main}" \
  "-Dpact.verifier.publishResults=$publish" \
  "-Dquarkus.profile=${PERFIL_QUARKUS_BUILD:-prod}"
python3 "$aula/scripts/artefatos.py" relatorio "$provider/target/surefire-reports" "$classe" "$criterio"
