# Starter em cópia de trabalho

Na raiz da aula, rode `./scripts/preparar-exercicio.sh 01`, `02` ou `03`. Entre no diretório indicado por `PROVIDER=`. A cópia inclui o projeto compilável, os testes e os pactos gerados pelos consumers. Use `PACTOS_PRONTOS=1` após rodar `./scripts/verificar-local.sh` para reaproveitar os artefatos recentes.

Com Docker, rode `docker compose run --rm java ./scripts/preparar-exercicio.sh 01`. Para testar a cópia com Docker, a partir da raiz use `docker compose run --rm java -c 'cd local/exercicio-01.XXXXXX/apuracao-horas && ./mvnw -B test'` substituindo `XXXXXX` pelo sufixo impresso.

Use uma cópia por exercício. O script mantém a cópia para edição e o projeto principal preserva a linha de base.
