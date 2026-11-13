#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/versao-ci.sh"
export PACT_PUBLICAR_RESULTADOS=true
modo=${1:-normal}
if [[ "$modo" == webhook ]]; then
  : "${PROVIDER_SHA:?O webhook deve informar o SHA do provider}"
  [[ "$APP_VERSION" == "$PROVIDER_SHA" ]] || {
    echo 'O checkout difere do SHA do evento' >&2
    exit 1
  }
fi
"$aula/scripts/verificar-provider-broker.sh" "$modo"
