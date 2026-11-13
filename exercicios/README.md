# Exercícios

Em duplas, registrem a previsão antes de executar. Usem [o starter](starter/README.md) para obter uma cópia isolada e os JSONs dos consumers.

1. [Adicionar sindicato](exercicio-01.md)
2. [Preservar dias trabalhados](exercicio-02.md)
3. [Restaurar a unidade](exercicio-03.md)

## Atividade curta A — 404 ou JSON incompleto?

Em `exemplos/modulo-ferias`, execute `./mvnw -B -Dtest=ModuloFeriasPactTest,ApuracaoClientRobustezTest test`. Ache o teste de `F404`, o caso de campo obrigatório ausente e o caso de zero explícito. Anote qual exceção ou retorno cada um produz. Depois troque localmente o `Integer` por `int` na cópia e investigue o que pode virar zero ao remover a validação de ausência. Preserve a declaração original ao terminar.

## Atividade curta B — Gate desconhecido

Execute `./scripts/verificar-broker.sh` na raiz da aula ou abra [as evidências](../evidencias/README.md). Identifique o SHA do provider já registrado e a versão da feature de férias. Compare o gate antes e depois do webhook. O que o pipeline deve fazer se o resultado continuar desconhecido após as tentativas? Qual versão do provider deve executar o teste?
