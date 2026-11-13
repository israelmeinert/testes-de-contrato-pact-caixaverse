#!/usr/bin/env python3
"""Valida artefatos de testes antes de usá-los no laboratório."""
import json
import os
import re
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

CONSUMERS = ("integracao-cliente", "modulo-ferias")
PROVIDER = "apuracao-horas"


def pacto(exemplos, consumer):
    pasta = exemplos / consumer / "target/pacts"
    esperado = pasta / f"{consumer}-{PROVIDER}.json"
    if list(pasta.glob("*.json")) != [esperado]:
        raise ValueError(f"Esperado somente {esperado}")
    dados = json.loads(esperado.read_text())
    par = (dados["consumer"]["name"], dados["provider"]["name"])
    if par != (consumer, PROVIDER):
        raise ValueError(f"Par inesperado: {par}")
    interacoes = dados["interactions"]
    esperado_por_status = {
        200: ("/apuracoes/F001/2026-10", "F001 apurado em 2026-10"),
        404: ("/apuracoes/F404/2026-10", "F404 sem apuracao em 2026-10"),
    }
    if len(interacoes) != 2 or {i["response"]["status"] for i in interacoes} != {200, 404}:
        raise ValueError(f"Esperadas duas interações, 200 e 404: {esperado}")
    for interacao in interacoes:
        caminho, estado = esperado_por_status[interacao["response"]["status"]]
        req = interacao["request"]
        if (req["method"], req["path"], req["headers"].get("Accept")) != ("GET", caminho, "application/json"):
            raise ValueError(f"Requisição inesperada: {esperado}")
        if interacao["providerStates"] != [{"name": estado}]:
            raise ValueError(f"Estado inesperado: {esperado}")
    return esperado


def copiar(exemplos):
    arquivos = [pacto(exemplos, consumer) for consumer in CONSUMERS]
    destino = exemplos / PROVIDER / "src/test/resources/pacts"
    destino.mkdir(parents=True, exist_ok=True)
    for antigo in destino.glob("*.json"):
        antigo.unlink()
    for arquivo in arquivos:
        shutil.copy2(arquivo, destino)
        print(f"Contrato conferido: {arquivo.name}")


def relatorio(raiz, classe, modo):
    relatorios = list(raiz.glob(f"TEST-*.{classe}.xml"))
    if len(relatorios) != 1:
        raise ValueError(f"Esperado relatório único de {classe} em {raiz}")
    casos = ET.parse(relatorios[0]).getroot().findall("testcase")
    if not casos or any(caso.find("error") is not None or caso.find("skipped") is not None for caso in casos):
        raise ValueError(f"Erro de execução ou teste ignorado em {classe}")
    saida = "\n".join(caso.findtext("system-out", default="") for caso in casos)
    if modo == "contar":
        for consumer in CONSUMERS:
            interacoes = re.findall(rf"Verifying a pact between {consumer}(?: \([^\n]*\))? and {PROVIDER}", saida)
            if len(interacoes) < 2:
                raise ValueError(f"Contrato esperado no Broker: {consumer}")
        if os.environ.get("PACT_PUBLICAR_RESULTADOS") == "true" and (
            saida.count("Published verification result of 'Ok(") * 2 < len(casos)
        ):
            raise ValueError("Faltam resultados publicados dos pactos verificados")
        if len(casos) < 4 or any(caso.find("failure") is not None for caso in casos):
            raise ValueError(f"Esperadas ao menos quatro interações bem-sucedidas em {classe}")
        print(f"{classe}: {len(casos)} interações verificadas")
        return
    if modo == "webhook":
        if len(casos) != 2 or any(caso.find("failure") is not None for caso in casos):
            raise ValueError(f"Esperadas duas interações do pacto recebido em {classe}")
        if os.environ.get("PACT_PUBLICAR_RESULTADOS") == "true" and "Published verification result of 'Ok(" not in saida:
            raise ValueError("O resultado do pacto exato não foi publicado")
        print(f"{classe}: {len(casos)} interações do pacto exato verificadas")
        return
    falhas = [caso for caso in casos if caso.find("failure") is not None]
    if modo == "unidade-funcional":
        if len(casos) != 3 or len(falhas) != 3 or any(
            "horasTrabalhadas em horas" not in (c.find("failure").get("message") or "") or
            not any(valor in (c.find("failure").get("message") or "") for valor in ("<9600>", "<9.6E+3>"))
            for c in falhas
        ):
            raise ValueError("Esperadas três falhas funcionais de 160 h versus 9600 min")
    else:
        if len(casos) != 4:
            raise ValueError("Esperadas exatamente quatro interações Pact")
        esperadas = {
            "ampla": {"integracao-cliente": "horasTrabalhadas", "modulo-ferias": "diasTrabalhados"},
            "dias": {"modulo-ferias": "diasTrabalhados"},
            "unidade": {},
            "compativel": {},
        }[modo]
        if len(falhas) != len(esperadas):
            raise ValueError(f"Número de incompatibilidades inesperado: {len(falhas)}")
        vistas = set()
        for caso in falhas:
            erro = caso.find("failure")
            detalhes = (erro.get("message") or "") + (erro.text or "") + caso.findtext("system-out", default="")
            matches = [consumer for consumer, campo in esperadas.items() if re.search(rf"Verifying a pact between {consumer}(?: \([^\n]*\))? and {PROVIDER}", detalhes) and campo in detalhes]
            if len(matches) != 1:
                raise ValueError(f"Falha diferente da prevista: {detalhes}")
            vistas.add(matches[0])
        if vistas != esperadas.keys():
            raise ValueError(f"Consumers afetados inesperados: {vistas}")
    print(f"{classe}: {len(casos)} casos, {len(falhas)} falhas previstas ({modo})")


if __name__ == "__main__":
    comando, caminho, *args = sys.argv[1:]
    try:
        if comando == "copiar":
            copiar(Path(caminho))
        elif comando == "pacto":
            print(pacto(Path(caminho), *args))
        elif comando == "relatorio":
            relatorio(Path(caminho), *args)
        else:
            raise ValueError(f"Comando desconhecido: {comando}")
    except (ValueError, OSError, KeyError, ET.ParseError) as exc:
        sys.exit(str(exc))
