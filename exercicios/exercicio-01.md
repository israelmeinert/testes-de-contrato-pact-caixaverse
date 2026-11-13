# Exercício 01 — Adicionar sindicato

## Contexto

O cadastro já afeta a jornada, mas a API ainda não expõe o sindicato.

## Objetivo

Adicionar informação ao JSON e verificar os clientes existentes.

## Tarefa

Na cópia 01, inclua um objeto `sindicato` com `codigo` no DTO de resposta e faça o mapeamento no Resource.

## Regras

Mantenha os nomes, tipos, unidades e valores das chaves de v1. Para quem não tem sindicato, envie `"sindicato": null`.

## Critérios de aceite

- A suíte Pact mantém quatro interações bem-sucedidas.
- F001 retorna sindicato `SIND-TI` e 160 horas, 8 extras e 20 dias.
- A resposta sem sindicato contém a chave com valor nulo.
- Os testes de robustez dos dois clients toleram o campo novo.

## Como executar

Use `./scripts/preparar-exercicio.sh 01` na raiz da aula e abra a cópia indicada.

## Como testar

Rode `./mvnw -B -Dtest=ApuracaoProviderContractTest test` antes da alteração e `./mvnw -B clean test` depois. Para conferir tolerância, rode `./scripts/testar-consumers.sh` a partir da raiz da aula.

## Resultado esperado

Os pactos passam antes e depois. O funcional passa com o novo campo presente.

## Dicas graduais

1. Veja onde os minutos são convertidos para horas.
2. A entidade de apuração já possui o código armazenado.
3. O DTO aninhado pode ter um único componente.

## Extensão opcional para alunos Pleno

Descreva a política de compatibilidade para um client que rejeita campos desconhecidos e proponha como confirmá-la antes de publicar.
