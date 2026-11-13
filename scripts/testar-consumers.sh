#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
for consumer in "${consumers[@]}"; do
  printf '\n=== %s ===\n' "$consumer"
  maven "$aula/exemplos/$consumer" clean verify
  python3 "$aula/scripts/artefatos.py" pacto "$aula/exemplos" "$consumer"
done
