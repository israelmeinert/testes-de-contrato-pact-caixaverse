# Solução 03 — Restaurar horas

Na raiz da aula, crie a cópia com `./scripts/preparar-exercicio.sh 03`. Ainda na raiz, aplique [o patch](solucao.patch) com `patch -d "$PROVIDER" -p1 < gabarito/exercicio-03/solucao.patch`, depois de definir `PROVIDER` com o caminho impresso pelo script. Execute `(cd "$PROVIDER" && ./mvnw -B clean test)`.

## Decisão

Converter os minutos do total com CalculoMensal.horas. A função usa BigDecimal com duas casas e HALF_UP. O total já inclui as horas extras.

## Alternativas e limites

Um mapeador dedicado ou conversão em factory do DTO pode preservar o mesmo contrato, com mais separação quando houver muitos endpoints.

O matcher numérico continua sem avaliar unidade. O teste HTTP de valor exato protege a semântica deste caso.

## Erros comuns

Somar horas extras ao total duplica parte do período. Usar divisão inteira perde a fração.

## Extensão

Acrescente um caso funcional de valor fracionário e uma API v2 com unidade no nome.
