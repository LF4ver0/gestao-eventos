# Gestão de Eventos - Backend

API REST para cadastro de **instituições** e **eventos**. Cada evento pertence a uma instituição (que pode ter vários) e possui uma vigência (`dataInicial` e `dataFinal`). Dentro da vigência o evento fica **ativo**; ao término dela é **inativado**.

## Tecnologias

- Java 24 (compilação) e Quarkus 3.40
- Hibernate ORM com Panache
- Hibernate Validator (Bean Validation)
- Quarkus Scheduler
- SmallRye OpenAPI / Swagger UI
- H2 (banco em memória)
- Lombok
- JUnit 5 e RestAssured

## Como executar

**Modo desenvolvimento** (requer JDK e Maven):

```bash
mvn quarkus:dev
```

**Testes:**

```bash
mvn test
```

Os testes sobem a aplicação na porta 8081. Encerre o modo desenvolvimento antes de rodá-los para evitar conflito de porta.

**Docker** (a partir da raiz do repositório, junto com o frontend):

```bash
docker compose up --build
```

A API sobe em `http://localhost:8081` e a documentação interativa em `http://localhost:8081/q/swagger-ui`.

## Estrutura

```
src/main/java/org/lfavero
├── application
│   ├── dto/request        EventoRequest, InstituicaoRequest
│   ├── dto/response       EventoResponse, InstituicaoResponse, ErroResponse
│   ├── exception          ApiException, ErrorMappers
│   ├── resource           EventoResource, InstituicaoResource
│   ├── scheduler          VigenciaScheduler
│   └── services           EventoService, InstituicaoService, VigenciaService
├── config                 ClockProducer
└── domain
    ├── entities           Evento, Instituicao
    ├── enums              TipoInstituicao
    └── repositories       EventoRepository, InstituicaoRepository
```

## Modelo de dados

| Instituicao | Evento |
|---|---|
| `id` INTEGER (chave primária) | `id` INTEGER (chave primária) |
| `nome` VARCHAR(150) | `nome` VARCHAR(150) |
| `tipo` VARCHAR(30) | `data_inicial` DATE |
| | `data_final` DATE |
| | `ativo` BOOLEAN |
| | `instituicao_id` INTEGER (chave estrangeira) |

- Relacionamento: uma instituição possui vários eventos (`@OneToMany` / `@ManyToOne`).
- `tipo` é um enum (`CONFEDERACAO`, `SINGULAR`, `CENTRAL`, `COOPERATIVA`) gravado como texto.
- Foram adicionados a chave estrangeira `instituicao_id` e índices em `ativo` e `instituicao_id`.
- O `import.sql` cadastra quatro instituições de exemplo ao iniciar.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/instituicoes` | Lista as instituições |
| GET | `/api/instituicoes/{id}` | Busca uma instituição |
| POST | `/api/instituicoes` | Cadastra (201 com `Location`) |
| PUT | `/api/instituicoes/{id}` | Atualiza |
| DELETE | `/api/instituicoes/{id}` | Exclui (204; 409 se houver eventos) |
| GET | `/api/eventos` | Lista eventos, com filtros opcionais `instituicaoId` e `ativo` |
| GET | `/api/eventos/{id}` | Busca um evento |
| POST | `/api/eventos` | Cadastra (201 com `Location`) |
| PUT | `/api/eventos/{id}` | Atualiza |
| DELETE | `/api/eventos/{id}` | Exclui (204) |
| POST | `/api/eventos/sincronizar-vigencia` | Executa manualmente a rotina de vigência |

Exemplo de corpo para criar um evento:

```json
{
  "nome": "Assembleia Geral",
  "dataInicial": "2026-12-01",
  "dataFinal": "2026-12-03",
  "instituicaoId": 1
}
```

As datas usam o formato ISO `yyyy-MM-dd`. O campo `ativo` não é enviado pelo cliente: é calculado pelo servidor. O endpoint de sincronização exige `Content-Type: application/json`.

## Decisões de projeto

### 1. Recebimento dos dados

As datas chegam como texto `yyyy-MM-dd` e são lidas como `LocalDate`, sem horário e sem fuso, o que evita deslocamento de um dia por conversão de fuso. O corpo da requisição é recebido em DTOs (`records`), e as entidades nunca são expostas na API.

### 2. Tratamento e validação

- **Bean Validation** nos DTOs: nome obrigatório (até 150 caracteres), datas obrigatórias, instituição obrigatória.
- **Regra cruzada** `dataFinal >= dataInicial`, reportada no campo `dataFinal`.
- **Início não pode ser anterior a hoje** no cadastro. Na edição, só é validado se a data inicial for alterada, para que um evento já em andamento continue editável.
- **Vigência** (`VigenciaService.estaVigente`): o evento está ativo quando `dataInicial <= hoje <= dataFinal`, com os dois extremos inclusivos. Ele passa a inativo a partir do dia seguinte à data final.
- **Fuso:** "hoje" vem de um `Clock` configurado em `eventos.timezone` (padrão `America/Sao_Paulo`), e não do fuso do servidor.
- No cadastro e na edição o campo `ativo` já é calculado na hora, sem depender do agendador.
- **Erros padronizados:**

```json
{
  "status": 400,
  "mensagem": "Dados inválidos",
  "erros": [{ "campo": "dataFinal", "mensagem": "A data final deve ser maior ou igual à data inicial" }]
}
```

| Situação | Status |
|---|---|
| Dados inválidos, JSON malformado, data fora do formato | 400 |
| Instituição ou evento não encontrado | 404 |
| Excluir instituição que possui eventos | 409 |

### 3. Ativação e inativação dos eventos

Foi usado **agendamento** (`@Scheduled`), e não mensageria. O gatilho é apenas a passagem do tempo, e todas as datas já são conhecidas no cadastro. Um sistema de mensageria exigiria um broker e ainda precisaria de um agendador para emitir a mensagem na virada do dia.

Todos os dias às 00:01 (e uma vez ao iniciar a aplicação) a rotina `VigenciaService.sincronizar()` executa duas atualizações em lote:

```sql
UPDATE evento SET ativo = true
 WHERE ativo = false AND data_inicial <= :hoje AND data_final >= :hoje;

UPDATE evento SET ativo = false
 WHERE ativo = true AND (data_final < :hoje OR data_inicial > :hoje);
```

Vantagens:

- **Idempotente:** compara o estado gravado com a data de hoje, então pode rodar várias vezes sem efeito colateral.
- **Resiliente:** se a aplicação ficar fora do ar na virada do dia, a execução no startup recupera o atraso.
- **Seguro com várias instâncias:** duas execuções simultâneas fazem o mesmo `UPDATE`, sem dano.
- **Eficiente:** duas instruções por dia, e não um temporizador por evento.

A mensageria passaria a fazer sentido se outros sistemas precisassem ser notificados quando um evento ativa ou inativa.

Configuração por variáveis de ambiente: `EVENTOS_TIMEZONE` (padrão `America/Sao_Paulo`) e `EVENTOS_VIGENCIA_CRON` (padrão `0 1 0 * * ?`).

### 4. Armazenamento

Persistência com JPA/Hibernate e Panache sobre **H2 em memória**. O esquema é recriado a cada inicialização (`drop-and-create`) e os dados são perdidos ao reiniciar a aplicação.

## Testes

16 testes automatizados:

- `VigenciaServiceTest`: regra de vigência (antes do início, dia inicial, dia final e dia seguinte ao fim).
- `EventoResourceTest`: evento que começa hoje nasce ativo, evento futuro nasce inativo, início no passado, data final anterior à inicial, campos obrigatórios, instituição inexistente e endpoint de sincronização.
- `InstituicaoResourceTest`: cadastro, nome vazio, instituição inexistente, exclusão sem eventos (204) e exclusão com eventos (409).
