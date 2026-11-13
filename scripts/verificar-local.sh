#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
export MAVEN_OPTS="${MAVEN_OPTS:-} -Djava.awt.headless=true"
"$aula/scripts/testar-consumers.sh"
"$aula/scripts/copiar-pactos.sh"
"$aula/scripts/testar-provider.sh"
