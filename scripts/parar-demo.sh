#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/processos-demo.sh"
mkdir -p "$estado"
exec 9>"$estado/.lock"
flock -x 9
for app in "${apps[@]}"; do
  parar_app "$app"
done
echo 'Processos do laboratório encerrados.'
