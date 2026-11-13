#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "${BASH_SOURCE[0]}")/ambiente.sh"

estado_git=$(git -C "$aula" status --porcelain --untracked-files=all)
if [[ -n $estado_git ]]; then
  echo 'A publicação exige checkout limpo. Commit ou descarte as mudanças no checkout apropriado.' >&2
  exit 1
fi
APP_VERSION=$(git -C "$aula" rev-parse HEAD)
APP_BRANCH=${APP_BRANCH:-${GITHUB_HEAD_REF:-${GITHUB_REF_NAME:-}}}
if [[ -z $APP_BRANCH ]]; then
  APP_BRANCH=$(git -C "$aula" symbolic-ref --short HEAD)
fi
git check-ref-format --branch "$APP_BRANCH" >/dev/null
export APP_VERSION APP_BRANCH
