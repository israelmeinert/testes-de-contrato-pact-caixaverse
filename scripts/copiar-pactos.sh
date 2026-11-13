#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
python3 "$aula/scripts/artefatos.py" copiar "$aula/exemplos"
