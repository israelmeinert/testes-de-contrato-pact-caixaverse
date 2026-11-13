#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/versao-ci.sh"
: "${PACTICIPANT:?Informe o pacticipant}"
: "${DEPLOY_ENVIRONMENT:?Informe o ambiente}"
: "${ARTEFATO_DIR:?Informe o diretório do artefato de CI}"
if [[ ${DEPLOY_ADAPTER:-} != ./* || ! -x "${DEPLOY_ADAPTER:-}" ]] ||
   ! git ls-files --error-unmatch -- "$DEPLOY_ADAPTER" >/dev/null 2>&1; then
  echo 'Integração de deploy pendente. Configure um adaptador executável versionado no repositório.' >&2
  exit 1
fi
[[ $(<"$ARTEFATO_DIR/versao") == "$APP_VERSION" && $(<"$ARTEFATO_DIR/pacticipant") == "$PACTICIPANT" ]]
(cd "$ARTEFATO_DIR" && sha256sum --check checksums.txt)
"$aula/scripts/gate.sh"
"$DEPLOY_ADAPTER" "$PACTICIPANT" "$APP_VERSION" "$DEPLOY_ENVIRONMENT" "$ARTEFATO_DIR/quarkus-app"
"$aula/scripts/broker-cli.sh" record-deployment --pacticipant "$PACTICIPANT" \
  --version "$APP_VERSION" --environment "$DEPLOY_ENVIRONMENT"
