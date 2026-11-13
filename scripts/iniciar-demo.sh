#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/processos-demo.sh"

mkdir -p "$estado"
exec 9>"$estado/.lock"
flock -x 9
for app in "${apps[@]}"; do
  parar_app "$app"
  maven "$aula/exemplos/$app" -Dquarkus.profile=demo -DskipTests package
done
trap 'for app in "${apps[@]}"; do parar_app "$app"; done' ERR
for app in "${apps[@]}"; do
  iniciar_app "$app" "$aula/exemplos/$app/target/quarkus-app"
  cenario_app "$app"
done
