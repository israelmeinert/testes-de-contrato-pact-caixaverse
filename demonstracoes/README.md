# Evolução da resposta do HoraCerta

Os patches partem da versão compatível de `exemplos/apuracao-horas`. O script prepara uma cópia descartável, executa os testes e interpreta o XML Surefire para distinguir incompatibilidade esperada de falhas de compilação, inicialização ou execução.

| Variante | Integração | Férias | Teste funcional |
|---|---|---|---|
| Compatível com sindicato | Passa | Passa | Confere 160 h e 8 h extras |
| `ampla.patch` | Falha, perdeu identificação e horas | Falha, perdeu identificação e dias | Cálculo permanece separado do DTO |
| `dias.patch` | Passa | Falha em `diasTrabalhados` | O formato público também muda |
| `unidade.patch` | Passa com `numberType` | Passa | Falha com 9600 em vez de 160 |
| `sem-sindicato.patch` | Passa | Passa | Starter do exercício 01, sem o campo novo |

`feature-sindicato.patch` parte de `exemplos/modulo-ferias`. A branch fictícia acrescenta um consumidor de `sindicato.codigo` para demonstrar que a publicação nova pode precisar de verificação específica. O laboratório gera o pacto a partir dos testes da branch.

Use `./scripts/demonstrar-quebra.sh ampla`, trocando o argumento por `dias`, `unidade` ou `compativel`. Com pactos recém-gerados pela suíte, exporte `PACTOS_PRONTOS=1` para aproveitar os JSONs. A cópia da demonstração é descartada ao final. Os logs ficam em `local/evidencias/` ou no caminho indicado por `EVIDENCIAS_DIR`.
