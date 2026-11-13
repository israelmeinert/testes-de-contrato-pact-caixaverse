#!/usr/bin/env bash
set -euo pipefail

aula=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
consumers=(integracao-cliente modulo-ferias)
apps=(apuracao-horas "${consumers[@]}")

maven() {
  local projeto=$1
  shift
  (cd "$projeto" && ./mvnw -B -ntp "$@")
}
