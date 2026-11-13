#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/versao-ci.sh"
app=${1:?Informe o pacticipant}
case "$app" in
  apuracao-horas|integracao-cliente|modulo-ferias) ;;
  *) exit 2 ;;
esac
destino="$aula/local/artefatos/$app"
test ! -e "$destino"
mkdir -p "$destino"
cp -a "$aula/exemplos/$app/target/quarkus-app" "$destino/"
(
  cd "$destino"
  find quarkus-app -type f -print0 | sort -z | xargs -0 sha256sum > checksums.txt
  printf '%s\n' "$APP_VERSION" > versao
  printf '%s\n' "$app" > pacticipant
)
