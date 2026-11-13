#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "${BASH_SOURCE[0]}")/ambiente.sh"

declare -A portas=(
  [apuracao-horas]=${APURACAO_PORT:-18080}
  [integracao-cliente]=${INTEGRACAO_PORT:-18081}
  [modulo-ferias]=${FERIAS_PORT:-18082}
)
estado=${ESTADO_DEMO:-"$aula/local/demo"}

url_app() {
  printf 'http://127.0.0.1:%s' "${portas[$1]}"
}

processo_ativo() {
  local pid=$1 jar=$2
  [[ "$pid" =~ ^[1-9][0-9]*$ && -r "/proc/$pid/cmdline" ]] &&
    tr '\0' '\n' < "/proc/$pid/cmdline" | grep -Fx -- "$jar" >/dev/null
}

parar_app() {
  local app=$1 pid jar
  [[ -f "$estado/$app.pid" ]] || return 0
  mapfile -t processo < "$estado/$app.pid"
  pid=${processo[0]} jar=${processo[1]}
  if processo_ativo "$pid" "$jar"; then
    kill "$pid"
    for ((tentativa=0; tentativa<50; tentativa++)); do
      if ! processo_ativo "$pid" "$jar"; then break; fi
      sleep 0.2
    done
    if processo_ativo "$pid" "$jar"; then
      echo "Processo $app ($pid) ainda está encerrando" >&2
      return 1
    fi
  fi
  rm -- "$estado/$app.pid"
}

iniciar_app() {
  local app=$1 artefato=$2 jar pid
  jar="$(realpath -- "$artefato")/quarkus-run.jar"
  test -f "$jar"
  parar_app "$app"
  python3 - "${portas[$app]}" <<'PY'
import socket
import sys
with socket.socket() as sock:
    sock.bind(("127.0.0.1", int(sys.argv[1])))
PY
  mkdir -p "$estado"
  nohup java "-Dquarkus.http.port=${portas[$app]}" "-Dapuracao.url=$(url_app apuracao-horas)" \
    -jar "$jar" 9>&- </dev/null >"$estado/$app.log" 2>&1 &
  pid=$!
  printf '%s\n%s\n' "$pid" "$jar" > "$estado/$app.pid"
  for ((tentativa=0; tentativa<90; tentativa++)); do
    if ! kill -0 "$pid" 2>/dev/null; then break; fi
    if curl -fsS --max-time 2 "$(url_app "$app")/q/health/ready" >/dev/null 2>&1; then
      processo_ativo "$pid" "$jar"
      printf '%s pronto em %s\n' "$app" "$(url_app "$app")"
      return 0
    fi
    sleep 1
  done
  tail -n 30 "$estado/$app.log" >&2
  return 1
}

cenario_app() {
  local app=$1 url caminho
  url=$(url_app "$app")
  case "$app" in
    apuracao-horas)
      caminho=/apuracoes/F001/2026-10
      curl -fsS --max-time 8 -X POST "$url$caminho/calcular" >/dev/null
      ;;
    integracao-cliente) caminho=/integracoes/F001/2026-10 ;;
    modulo-ferias) caminho=/ferias/frequencia/F001/2026-10 ;;
  esac
  curl -fsS --max-time 8 "$url$caminho" | python3 -c '
import json, sys
app = sys.argv[1]
dados = json.load(sys.stdin)
assert dados["funcionarioId"] == "F001" and dados["competencia"] == "2026-10"
if app != "modulo-ferias":
    assert dados["horasTrabalhadas"] == 160 and dados["horasExtras"] == 8
if app != "integracao-cliente":
    assert dados["diasTrabalhados"] == 20
if app == "apuracao-horas":
    assert dados["sindicato"]["codigo"] == "SIND-TI"
print(app, json.dumps(dados, ensure_ascii=False))' "$app"
}
