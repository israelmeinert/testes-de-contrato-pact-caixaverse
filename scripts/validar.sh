#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
cd "$aula"
export LAB_UID=${LAB_UID:-$(id -u)} LAB_GID=${LAB_GID:-$(id -g)}
docker compose config --quiet
docker compose -f infra/docker-compose.yml config --quiet
docker compose run --build --rm java
for variante in ampla dias unidade; do
  docker compose run --rm -e PACTOS_PRONTOS=1 java ./scripts/demonstrar-quebra.sh "$variante"
done
./scripts/renderizar-slides.sh
if [[ ${VALIDAR_BROKER:-0} == 1 ]]; then
  ./scripts/verificar-broker.sh
fi
