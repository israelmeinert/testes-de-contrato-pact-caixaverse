# Solução 02 — Preservar dias

Na raiz da aula, crie a cópia com `./scripts/preparar-exercicio.sh 02`. Ainda na raiz, aplique [o patch](solucao.patch) com `patch -d "$PROVIDER" -p1 < gabarito/exercicio-02/solucao.patch`, depois de definir `PROVIDER` com o caminho impresso pelo script. Execute `(cd "$PROVIDER" && ./mvnw -B clean test)`.

## Decisão

Restaurar o componente diasTrabalhados no record de resposta. A única incompatibilidade Pact observada está no 200 de férias.

## Alternativas e limites

Uma anotação JsonProperty pode manter o nome HTTP quando o Java usa outro nome. Um campo adicional também funciona durante migração.

Um consumer novo pode adotar a chave adicional, o que cria um novo compromisso de suporte.

## Erros comuns

Alterar o contrato de férias apenas para encobrir o rename deixa versões já implantadas quebradas.

## Extensão

Proponha uma transição com métricas de uso e verificação de versões coexistentes.
