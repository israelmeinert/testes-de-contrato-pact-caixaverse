#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "$0")/ambiente.sh"
cd "$aula"
export LAB_UID=${LAB_UID:-$(id -u)} LAB_GID=${LAB_GID:-$(id -g)}
marp=(docker compose run --rm --no-deps marp /opt/slides/node_modules/@marp-team/marp-cli/marp-cli.js)
docker compose build marp
"${marp[@]}" apresentacao.md --html --output index.html
"${marp[@]}" apresentacao.md --html --allow-local-files --pdf --pdf-outlines --output apresentacao.pdf
docker compose run --rm --no-deps marp /opt/slides/verificar-slides.mjs
