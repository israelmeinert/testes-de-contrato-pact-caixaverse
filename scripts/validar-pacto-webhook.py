#!/usr/bin/env python3
"""Confere o endereço e o par do pacto antes de iniciar o verifier."""
import base64
import json
import os
import re
import sys
from urllib.error import HTTPError
from urllib.parse import urlsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener


class SemRedirect(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def origem(url):
    if url.scheme not in ("http", "https") or not url.hostname or url.username is not None:
        raise ValueError("URL inválida")
    return url.scheme, url.hostname, url.port or (443 if url.scheme == "https" else 80)


def main():
    broker = urlsplit(os.environ["PACT_BROKER_BASE_URL"])
    endereco = os.environ["PACT_WEBHOOK_URL"]
    pacto = urlsplit(endereco)
    consumer = os.environ["PACT_WEBHOOK_CONSUMER"]
    if consumer not in {"integracao-cliente", "modulo-ferias"}:
        raise ValueError("Consumer desconhecido")
    caminho = f"{broker.path.rstrip('/')}/pacts/provider/apuracao-horas/consumer/{consumer}/"
    if (origem(broker) != origem(pacto) or pacto.query or pacto.fragment or
            not re.fullmatch(re.escape(caminho) + r"(?:pact-version|version)/[a-zA-Z0-9._-]+", pacto.path) or
            pacto.path.rsplit("/", 1)[-1] in {".", "..", "latest"}):
        raise ValueError("O webhook exige URL versionada do par esperado na origem do Broker")
    credencial = os.environ["PACT_BROKER_USERNAME"] + ":" + os.environ["PACT_BROKER_PASSWORD"]
    token = base64.b64encode(credencial.encode()).decode("ascii")
    req = Request(endereco, headers={"Authorization": f"Basic {token}", "Accept": "application/json"})
    with build_opener(SemRedirect()).open(req, timeout=10) as resposta:
        contrato = json.load(resposta)
    if contrato["consumer"]["name"] != consumer or contrato["provider"]["name"] != "apuracao-horas":
        raise ValueError("Pacto de outro par")
    print(f"Pacto exato para {consumer}: {endereco}")


if __name__ == "__main__":
    try:
        main()
    except HTTPError as erro:
        sys.exit(f"Consulta do pacto falhou com HTTP {erro.code}")
    except (ValueError, KeyError, OSError) as erro:
        sys.exit(f"Falha na validação do pacto: {erro}")
