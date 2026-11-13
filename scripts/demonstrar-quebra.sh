#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
modo=${1:-}
case "$modo" in
  ampla|dias|unidade|compativel) ;;
  *) echo 'Uso: demonstrar-quebra.sh ampla|dias|unidade|compativel' >&2; exit 2 ;;
esac

if [[ ${PACTOS_PRONTOS:-0} != 1 ]]; then
  "$aula/scripts/testar-consumers.sh"
fi
copia=$(mktemp -d "${TMPDIR:-/tmp}/horacerta-$modo.XXXXXX")
trap 'rm -rf -- "$copia"' EXIT
provider="$copia/apuracao-horas"
for consumer in "${consumers[@]}"; do
  arquivo=$(python3 "$aula/scripts/artefatos.py" pacto "$aula/exemplos" "$consumer")
  mkdir -p "$copia/$consumer/target/pacts"
  cp -- "$arquivo" "$copia/$consumer/target/pacts/"
done
mkdir -p "$provider"
tar -C "$aula/exemplos/apuracao-horas" --exclude=target -cf - . | tar -C "$provider" -xf -
python3 "$aula/scripts/artefatos.py" copiar "$copia"
if [[ "$modo" != compativel ]]; then
  patch --batch --forward --fuzz=0 -d "$provider" -p1 < "$aula/demonstracoes/$modo.patch"
fi
logs=${EVIDENCIAS_DIR:-"$aula/local/evidencias"}
mkdir -p "$logs"

observar() {
  local teste=$1 expectativa=$2 saida_esperada=$3
  local log="$logs/$modo-$expectativa.log" status=0
  maven "$provider" -Dtest="$teste" clean test > "$log" 2>&1 || status=$?
  printf '%s / %s: Maven saiu com %s (esperado %s)\n' "$modo" "$teste" "$status" "$saida_esperada" | tee -a "$log"
  if [[ "$status" != "$saida_esperada" ]]; then
    tail -n 65 "$log" >&2
    return 1
  fi
  python3 "$aula/scripts/artefatos.py" relatorio "$provider/target/surefire-reports" "${teste%%#*}" "$expectativa" | tee -a "$log"
}

case "$modo" in
  ampla|dias) observar ApuracaoProviderContractTest "$modo" 1 ;;
  compativel|unidade) observar ApuracaoProviderContractTest "$modo" 0 ;;
esac
if [[ "$modo" == unidade ]]; then
  observar 'ApuracaoResourceFuncionalTest#calculaUnidadeExata' unidade-funcional 1
fi
