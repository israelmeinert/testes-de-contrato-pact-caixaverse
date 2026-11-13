#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
numero=${1:-}
case "$numero" in
  01) variante=sem-sindicato ;;
  02) variante=dias ;;
  03) variante=unidade ;;
  *) echo 'Uso: preparar-exercicio.sh 01|02|03' >&2; exit 2 ;;
esac

if [[ ${PACTOS_PRONTOS:-0} != 1 ]]; then
  "$aula/scripts/testar-consumers.sh"
fi
mkdir -p "$aula/local"
diretorio=$(mktemp -d "$aula/local/exercicio-$numero.XXXXXX")
provider="$diretorio/apuracao-horas"
mkdir -p "$provider"
tar -C "$aula/exemplos/apuracao-horas" --exclude=target -cf - . | tar -C "$provider" -xf -
for consumer in "${consumers[@]}"; do
  arquivo=$(python3 "$aula/scripts/artefatos.py" pacto "$aula/exemplos" "$consumer")
  mkdir -p "$diretorio/$consumer/target/pacts"
  cp -- "$arquivo" "$diretorio/$consumer/target/pacts/"
done
python3 "$aula/scripts/artefatos.py" copiar "$diretorio"
patch --batch --fuzz=0 -d "$provider" -p1 < "$aula/demonstracoes/$variante.patch"
printf 'PROVIDER=%s\n' "$provider"
