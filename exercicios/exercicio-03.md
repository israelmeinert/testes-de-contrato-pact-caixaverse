# Exercício 03 — Restaurar horas na v1

## Contexto

A API retorna 9600 na propriedade `horasTrabalhadas`. O valor representa minutos.

## Objetivo

Demonstrar a diferença entre compatibilidade por tipo e correção semântica.

## Tarefa

Na cópia 03, rode Pact e o teste funcional exato. Corrija a conversão no DTO de saída.

## Regras

O armazenamento continua em minutos. O total trabalhado inclui o tempo extra.

## Critérios de aceite

- O Pact passa mesmo no estado inicial.
- O funcional inicialmente acusa 160 versus 9600.
- Após a correção, F001 entrega 160 h, 8 h extras e 20 dias. SIND-LOG entrega 20 h extras.

## Como executar

Use `./scripts/preparar-exercicio.sh 03` e entre na cópia.

## Como testar

Rode `./mvnw -B -Dtest=ApuracaoProviderContractTest test`, depois `./mvnw -B '-Dtest=ApuracaoResourceFuncionalTest#calculaUnidadeExata' test`. Termine com `./mvnw -B clean test`.

## Resultado esperado

O funcional falha antes da correção e passa depois. O teste Pact mostra que o tipo numérico foi preservado.

## Dicas graduais

1. Ache a linha responsável por `horasTrabalhadas`.
2. Compare com o mapeamento de `horasExtras`.
3. Reaproveite o método de conversão do cálculo mensal.

## Extensão opcional para alunos Pleno

Proponha um campo explícito em minutos para v2. Discuta quando um matcher por valor faria sentido em um caso diferente.
