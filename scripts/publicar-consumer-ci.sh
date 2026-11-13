#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/versao-ci.sh"
consumer=${1:-}
case "$consumer" in
  integracao-cliente|modulo-ferias) ;;
  *) echo 'Informe integracao-cliente ou modulo-ferias' >&2; exit 2 ;;
esac
maven "$aula/exemplos/$consumer" clean verify
python3 "$aula/scripts/artefatos.py" pacto "$aula/exemplos" "$consumer"
source "$aula/scripts/versao-ci.sh"
cd "$aula/exemplos/$consumer"
"$aula/scripts/broker-cli.sh" publish "target/pacts/$consumer-apuracao-horas.json" \
  --consumer-app-version "$APP_VERSION" --branch "$APP_BRANCH"
