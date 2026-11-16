# Gestão de Eventos

Sistema para cadastro de **instituições** e **eventos**. Cada evento pertence a uma instituição e tem uma vigência (data inicial e data final): dentro dela o evento fica **ativo** automaticamente e, ao término, é **inativado**.

## Estrutura

| Pasta | Conteúdo |
|---|---|
| [`backend/gestao-eventos`](backend/gestao-eventos/README.md) | API REST em Java com Quarkus |
| [`frontend`](frontend/README.md) | Interface web em React com TypeScript |

As decisões técnicas e as respostas às questões do desafio estão em [Justificativa das decisões do sistema Gestão de Eventos](<Justificativa das decisões do sistema Gestão de Eventos.md>).

## Executando com Docker

Requer Docker com Docker Compose. Na raiz do repositório:

```bash
docker compose up --build
```

| Serviço | Endereço |
|---|---|
| Aplicação (frontend) | http://localhost:5173 |
| API (backend) | http://localhost:8081 |
| Documentação da API (Swagger) | http://localhost:8081/q/swagger-ui |

Para encerrar, use `Ctrl+C` e, em seguida, `docker compose down`.

O banco é H2 em memória: os dados cadastrados são perdidos quando os contêineres são reiniciados. Quatro instituições de exemplo são criadas ao iniciar.

## Executando sem Docker

Os passos de cada parte estão nos READMEs do [backend](backend/gestao-eventos/README.md) e do [frontend](frontend/README.md). Resumo:

```bash
cd backend/gestao-eventos
mvn quarkus:dev
```

```bash
cd frontend
npm install
npm run dev
```

## Regras principais

- O evento está ativo quando `dataInicial <= hoje <= dataFinal` (extremos inclusivos).
- O cadastro só aceita eventos com data inicial a partir de hoje, e a data final deve ser maior ou igual à inicial.
- O status é calculado pelo servidor e atualizado todos os dias às 00:01 por uma rotina agendada, que também roda ao iniciar a aplicação.
- Uma instituição que possui eventos não pode ser excluída.
