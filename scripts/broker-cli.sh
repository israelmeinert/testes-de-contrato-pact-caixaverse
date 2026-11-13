#!/usr/bin/env bash
set -euo pipefail
: "${PACT_BROKER_BASE_URL:?Informe a URL do Broker}"
: "${PACT_BROKER_USERNAME:?Informe o usuário basic auth}"
: "${PACT_BROKER_PASSWORD:?Informe a senha basic auth}"
if command -v pact-broker >/dev/null; then
  exec pact-broker "$@"
fi
exec docker run --rm --network host \
  --env PACT_BROKER_BASE_URL --env PACT_BROKER_USERNAME --env PACT_BROKER_PASSWORD \
  --mount "type=bind,source=$PWD,target=/projeto,readonly" --workdir /projeto \
  pactfoundation/pact-cli:1.5.0.5 pact-broker "$@"
