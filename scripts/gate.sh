#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
: "${PACTICIPANT:?Informe o pacticipant}"
: "${APP_VERSION:?Informe a versão a implantar}"
: "${DEPLOY_ENVIRONMENT:?Informe o ambiente}"
case "$PACTICIPANT" in
  apuracao-horas|integracao-cliente|modulo-ferias) ;;
  *) echo 'Pacticipant desconhecido' >&2; exit 2 ;;
esac
if [[ $# -ne 0 && ( $# -ne 2 || $1 != --output || $2 != json ) ]]; then
  echo 'O gate aceita apenas a opção --output json' >&2
  exit 2
fi
unset PACT_BROKER_CAN_I_DEPLOY_IGNORE PACT_BROKER_CAN_I_DEPLOY_DRY_RUN
exec "$aula/scripts/broker-cli.sh" can-i-deploy --no-dry-run \
  --pacticipant "$PACTICIPANT" --version "$APP_VERSION" --to-environment "$DEPLOY_ENVIRONMENT" \
  --retry-while-unknown "${PACT_GATE_RETRIES:-12}" --retry-interval "${PACT_GATE_INTERVAL:-5}" "$@"
