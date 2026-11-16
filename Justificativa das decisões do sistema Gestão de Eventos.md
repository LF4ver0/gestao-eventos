# Justificativa das decisões do sistema Gestão de Eventos

## Visão geral

O sistema tem um **frontend** em React com TypeScript e um **backend** em Java com Quarkus. O backend usa Hibernate/Panache sobre H2. Cada evento tem uma vigência (`dataInicial` e `dataFinal`) e pertence a uma instituição. O estado do evento (ativo ou inativo) **não é informado pelo usuário**: ele é sempre calculado pelo servidor a partir da vigência.

---

## 1. Recebimento de dados

**Interface**

- Os campos de vigência usam `<input type="date">`. Ele entrega o valor como texto no formato `yyyy-MM-dd`, sem horário e sem fuso, e oferece um calendário nativo.
- O formulário de evento tem nome, instituição (lista de seleção), data inicial e data final. Não existe campo de status, porque ele é derivado.
- O calendário da data inicial desabilita os dias anteriores a hoje, tanto no cadastro quanto na edição. O da data final desabilita os dias anteriores à data inicial. O usuário não consegue nem selecionar uma data inválida.
- O formulário valida na hora: campos obrigatórios, data final maior ou igual à inicial e data inicial não anterior a hoje. Na edição de um evento já em andamento, a data inicial original continua aceita se não for alterada.
- Há indicadores de carregamento nas listagens e ao lado do botão de salvar, para dar retorno ao usuário.

**Garantia de que os dados chegam corretamente**

- A mesma string `yyyy-MM-dd` segue no JSON e é lida no backend como `LocalDate` (ISO-8601). Como não há horário nem fuso no caminho, o problema comum de a data "voltar um dia" não ocorre.
- As datas nunca passam por `new Date("yyyy-MM-dd")` no frontend, porque o JavaScript interpretaria o texto como UTC e, no Brasil, exibiria o dia anterior. A exibição em `dd/mm/aaaa` é feita separando o texto.
- O cliente não envia o campo `ativo`, e o backend o ignora mesmo que seja enviado.
- A validação do formulário serve para dar retorno imediato ao usuário. A validação que vale é a do backend, e os erros que ele devolve aparecem junto ao campo correspondente.

---

## 2. Tratamento e validação dos dados no backend

**Entrada**

- A API recebe DTOs (`records`) e nunca expõe as entidades.
- **Bean Validation:** nome obrigatório e de até 150 caracteres, datas obrigatórias e instituição obrigatória.
- **Regra cruzada:** `dataFinal >= dataInicial`, reportada no campo `dataFinal`.
- **Início não anterior a hoje:** vale no cadastro. Na edição, só é exigido se a data inicial for alterada, para que um evento em andamento continue editável.
- **Formato:** data fora de `yyyy-MM-dd`, enum desconhecido ou JSON malformado retornam 400 com mensagem clara.

**Integridade**

- Instituição ou evento inexistente: 404.
- Excluir uma instituição que possui eventos: 409.

**Formato único de erro**

```json
{
  "status": 400,
  "mensagem": "Dados inválidos",
  "erros": [
    { "campo": "dataFinal", "mensagem": "A data final deve ser maior ou igual à data inicial" }
  ]
}
```

O nome do campo permite ao frontend mostrar cada mensagem no input certo.

**Vigência ativa e inativa**

- A regra está em um único lugar (`VigenciaService.estaVigente`): o evento está **ativo** quando `dataInicial <= hoje <= dataFinal`, com os dois extremos inclusivos. Ele passa a **inativo** a partir das 00:00 do dia seguinte à data final.
- **Fuso:** "hoje" vem de um `Clock` configurado em `eventos.timezone` (padrão `America/Sao_Paulo`), e não do fuso do servidor ou da JVM. Isso evita resultados diferentes conforme o ambiente em que a aplicação roda.
- No cadastro e na edição o `ativo` já é calculado na hora. Um evento que começa hoje nasce ativo, e um que começa no futuro nasce inativo, sem esperar nenhuma rotina.

---

## 3. Ativação e inativação dos eventos

**Decisão: agendamento de tarefas, e não mensageria.**

Todos os dias às 00:01, e também uma vez ao iniciar a aplicação, uma rotina (`VigenciaService.sincronizar()`) executa duas atualizações em lote:

```sql
UPDATE evento SET ativo = true
 WHERE ativo = false AND data_inicial <= :hoje AND data_final >= :hoje;

UPDATE evento SET ativo = false
 WHERE ativo = true AND (data_final < :hoje OR data_inicial > :hoje);
```

Há ainda o endpoint `POST /api/eventos/sincronizar-vigencia`, que dispara a mesma rotina manualmente, útil para demonstração e operação. O horário e o fuso são configuráveis por variáveis de ambiente.

**Por que agendamento**

- **O gatilho é o tempo, e é previsível.** Todas as datas são conhecidas no cadastro, e não existe evento externo a ser reagido. Um broker de mensagens resolveria um problema que não existe no escopo atual do projeto. Além disso, a mensageria ainda precisaria de um agendador para emitir a mensagem na virada do dia, então o agendador continuaria sendo necessário e a mensageria seria uma peça a mais.
- **Idempotente.** A rotina compara o estado gravado com a data de hoje, e não "processa a virada do dia". Pode rodar várias vezes sem efeito colateral.
- **Resiliente a falhas.** Se a aplicação estiver fora do ar à meia-noite, a execução no startup recupera o atraso. Uma abordagem baseada em um evento disparado uma única vez perderia essa virada.
- **Segura com várias instâncias.** Duas execuções simultâneas fazem o mesmo `UPDATE`, sem causar dano.
- **Eficiente.** São duas instruções por dia, e não um temporizador por evento.
- **Simples de operar.** Não exige infraestrutura adicional.

**Quando eu evoluiria para mensageria**

Se outros sistemas precisassem ser notificados quando um evento ativa ou inativa (e-mails, integrações, auditoria), eu adicionaria mensageria sem trocar o agendador. A rotina continuaria decidindo o estado e, a cada mudança, publicaria um evento de domínio (por exemplo, EventoAtivado e EventoInativado) em um broker. O RabbitMQ seria a minha escolha: é simples de operar, adequado a um volume baixo como este e tem roteamento flexível, de modo que cada sistema interessado consome a mesma notificação em sua própria fila e de forma independente.

---

## 4. Armazenamento dos dados

**Modelo** (atributos do diagrama, mais o necessário para o relacionamento)

| Instituicao | Evento |
|---|---|
| `id` INTEGER (chave primária) | `id` INTEGER (chave primária) |
| `nome` VARCHAR(150) | `nome` VARCHAR(150) |
| `tipo` VARCHAR(30) | `data_inicial` DATE |
| | `data_final` DATE |
| | `ativo` BOOLEAN |
| | `instituicao_id` INTEGER (chave estrangeira) |

**Relacionamento:** uma instituição possui um ou mais eventos (`@OneToMany` na instituição e `@ManyToOne` no evento). A chave estrangeira `instituicao_id` é obrigatória, então um evento nunca fica sem instituição.

**Decisões**

- **`tipo` como enum** (`CONFEDERACAO`, `SINGULAR`, `CENTRAL`, `COOPERATIVA`), gravado como texto. Isso garante que só valores válidos sejam aceitos e mantém o dado legível no banco, ao contrário de gravar a posição numérica do enum, que quebraria se a ordem mudasse. Como melhoria futura, se a quantidade de tipos começar a crescer, uma opção é criar uma tabela separada de tipos e relacioná-la à instituição por chave estrangeira. Assim, novos tipos poderiam ser cadastrados sem alterar o código.
- **Datas como `DATE`** (`LocalDate`), sem horário, coerente com a vigência definida por dia.
- **`ativo` gravado no banco**, e não apenas calculado nas consultas. Isso permite filtrar e indexar por status, e o diagrama já previa esse atributo. A rotina agendada mantém o valor em dia.
- **Atributos adicionados:** a chave estrangeira `instituicao_id` e índices em `ativo` e `instituicao_id`, para acelerar os filtros da listagem.
- **Integridade na exclusão:** uma instituição com eventos não pode ser excluída (resposta 409), o que evita eventos órfãos e perda acidental de dados.
- **Banco H2 em memória**, de acordo com o escopo do desafio técnico. O esquema é criado a cada inicialização e quatro instituições de exemplo são carregadas. Como o acesso é feito por JPA, migrar para um banco servidor (como PostgreSQL) seria basicamente uma mudança de configuração. Em um ambiente real eu também usaria migrações versionadas do esquema, com Flyway ou Liquibase.

---

## Qualidade

O backend tem 16 testes automatizados que cobrem a regra de vigência (inclusive o último dia), o cadastro de eventos (começando hoje, no futuro e no passado), as validações, os códigos 404 e 409 e a rotina de sincronização. A aplicação completa também sobe com um único comando (`docker compose up --build`).
