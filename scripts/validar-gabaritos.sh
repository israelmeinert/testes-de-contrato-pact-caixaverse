#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
for numero in 01 02 03; do
  preparar=$(PACTOS_PRONTOS=1 "$aula/scripts/preparar-exercicio.sh" "$numero")
  printf '%s\n' "$preparar"
  provider=${preparar##*$'\n'PROVIDER=}
  patch --batch --fuzz=0 -d "$provider" -p1 < "$aula/gabarito/exercicio-$numero/solucao.patch"
  maven "$provider" clean test
  python3 "$aula/scripts/artefatos.py" relatorio "$provider/target/surefire-reports" ApuracaoProviderContractTest compativel
done
