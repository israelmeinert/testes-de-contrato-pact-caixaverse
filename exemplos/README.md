# Projetos executáveis

| Projeto | Responsabilidade | Teste principal |
|---|---|---|
| [apuracao-horas](apuracao-horas/pom.xml) | Consultar e recalcular o resumo persistido | JUnit de cálculo, HTTP funcional e verifier Pact |
| [integracao-cliente](integracao-cliente/pom.xml) | Exportar identificação e horas | Pact e robustez do client real |
| [modulo-ferias](modulo-ferias/pom.xml) | Consultar frequência | Pact e robustez do client real |

Cada consumer roda `./mvnw -B clean verify` por conta própria. Para o provider local, gere e copie os dois pactos com `./scripts/verificar-local.sh` na raiz da aula. Para testar só a apuração sem Pact, execute `./mvnw -B '-Dtest=*CalculoTest,*FuncionalTest' test` dentro do provider.

A operação de cálculo usa lock pessimista no funcionário para serializar o primeiro fechamento simultâneo e os recálculos. A restrição de unicidade protege o par funcionário e competência. Um teste faz dois POSTs concorrentes e confere uma única linha. Essa estratégia pode serializar competências distintas do mesmo funcionário. O laboratório usa um único banco H2 descartável.

A progressão começa com a resposta v1 sem sindicato, passa pela [quebra ampla](../demonstracoes/README.md) e termina na correção compatível padrão. Os patches isolam a mudança de dias e a troca de unidade.
