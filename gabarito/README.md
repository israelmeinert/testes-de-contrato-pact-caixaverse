# Gabaritos

Cada patch parte da cópia inicial do exercício correspondente e restaura o resultado esperado. Os testes automatizados são os mesmos que acompanham o starter.

- [Adicionar sindicato](exercicio-01/README.md)
- [Preservar dias](exercicio-02/README.md)
- [Restaurar horas](exercicio-03/README.md)

Após gerar pactos com `./scripts/verificar-local.sh`, use `./scripts/validar-gabaritos.sh` para aplicar e testar os três patches.

**Atividade A.** HTTP 404 gera `ApuracaoNaoEncontradaException`, o corpo 200 com campo ausente gera `IntegracaoException`, e zero explícito é um resumo válido. `Integer` permite distinguir ausência de zero no DTO.

**Atividade B.** O deploy permanece bloqueado enquanto a matriz carecer de resultado. O job roda o SHA provider já implantado contra a URL exata do pacto da feature. Se a verificação falhar, a equipe precisa negociar compatibilidade ou a ordem de implantação.
