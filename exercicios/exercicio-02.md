# Exercício 02 — Preservar dias trabalhados

## Contexto

A API passou a escrever `diasComputaveis` onde férias espera `diasTrabalhados`.

## Objetivo

Diagnosticar qual consumer quebrou e restaurar a compatibilidade do JSON.

## Tarefa

Na cópia 02, execute o verifier, encontre o campo faltante e corrija a resposta pública.

## Regras

Mantenha os contratos dos consumers. O cálculo interno e os totais já estão corretos.

## Critérios de aceite

- Antes da correção, férias falha no 200 e integração passa.
- Depois, o 200 de férias volta a incluir `diasTrabalhados: 20`.
- Os 404 e o funcional continuam passando.

## Como executar

Use `./scripts/preparar-exercicio.sh 02` e entre na cópia.

## Como testar

Execute `./mvnw -B -Dtest=ApuracaoProviderContractTest test` antes e depois. Ao terminar, execute `./mvnw -B clean test`.

## Resultado esperado

Uma falha Pact fica verde após restaurar a chave usada por férias.

## Dicas graduais

1. Leia o par de participantes no log.
2. Compare a propriedade esperada com o record serializado.
3. A chave pública pode permanecer estável enquanto o nome interno evolui.

## Extensão opcional para alunos Pleno

Desenhe uma migração que ofereça os dois campos enquanto houver versões antigas suportadas. Que evidência autoriza remover a chave v1?
