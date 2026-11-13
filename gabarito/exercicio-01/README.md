# Solução 01 — Adicionar sindicato

Na raiz da aula, crie a cópia com `./scripts/preparar-exercicio.sh 01`. Ainda na raiz, aplique [o patch](solucao.patch) com `patch -d "$PROVIDER" -p1 < gabarito/exercicio-01/solucao.patch`, depois de definir `PROVIDER` com o caminho impresso pelo script. Execute `(cd "$PROVIDER" && ./mvnw -B clean test)`.

## Decisão

Preservar o record público com as cinco chaves e acrescentar SindicatoResponse. O mapeamento usa o código da apuração e retorna null quando ausente.

## Alternativas e limites

Uma classe DTO equivalente também funciona. Um mapa permitiria a mesma forma de JSON, mas perderia a declaração estática dos campos.

Os pactos antigos cobrem apenas campos usados. Inspecione também os testes de robustez dos dois clients.

## Erros comuns

Serializar a entidade expõe detalhes do armazenamento. Omitir a chave quando ela é nula viola o requisito da atividade.

## Extensão

Adicione um novo consumer que leia sindicato e publique sua expectativa.
