#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
maven "$aula/exemplos/apuracao-horas" clean verify
python3 "$aula/scripts/artefatos.py" relatorio "$aula/exemplos/apuracao-horas/target/surefire-reports" ApuracaoProviderContractTest compativel
