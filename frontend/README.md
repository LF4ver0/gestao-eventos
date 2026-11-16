# Gestão de Eventos - Frontend

Interface web para cadastro e consulta de **instituições** e **eventos**, construída com React e TypeScript. Consome a API do backend.

## Tecnologias

- React 18 e TypeScript 5
- Vite 5
- CSS puro, sem bibliotecas de interface
- Nginx (somente na imagem Docker)

## Como executar

**Modo desenvolvimento** (requer Node 18 ou superior e o backend rodando em `http://localhost:8081`):

```bash
npm install
npm run dev
```

A aplicação abre em `http://localhost:5173`. O Vite encaminha as chamadas de `/api` para o backend (configurado em `vite.config.ts`), o que evita a necessidade de CORS.

**Build de produção:**

```bash
npm run build
```

**Docker** (a partir da raiz do repositório, junto com o backend):

```bash
docker compose up --build
```

Nessa imagem o Nginx serve os arquivos gerados e encaminha `/api` para o contêiner do backend. A aplicação fica em `http://localhost:5173`.

## Estrutura

```
src
├── api
│   ├── client.ts          Chamadas à API e tratamento de erros
│   └── types.ts           Tipos que espelham os DTOs do backend
├── pages
│   ├── EventosPage.tsx
│   └── InstituicoesPage.tsx
├── utils
│   └── datas.ts           Formatação e data de hoje
├── App.tsx                Cabeçalho, abas e título da página
├── main.tsx
└── styles.css
```

## Funcionalidades

- Abas para alternar entre **Eventos** e **Instituições**.
- Cadastro, edição e exclusão (com confirmação) de instituições e de eventos.
- Listagem de eventos com filtro por instituição e por status (todos, ativos, inativos), e indicação visual de ativo ou inativo.
- Indicador de carregamento nas listagens e ao lado do botão de salvar.
- Mensagens de erro exibidas junto ao campo correspondente, inclusive as devolvidas pelo servidor.

## Decisões de projeto

### Recebimento das datas

- Os campos de data usam `<input type="date">`, que entrega o valor como texto `yyyy-MM-dd`. Esse mesmo texto segue no JSON e é lido no backend como `LocalDate`, sem horário e sem fuso.
- As datas nunca passam por `new Date("yyyy-MM-dd")`, porque o JavaScript interpretaria o texto como UTC e, no Brasil, exibiria o dia anterior. A exibição (`dd/mm/aaaa`) é feita separando o texto.
- O status (ativo ou inativo) não faz parte do formulário. Ele é calculado pelo backend a partir da vigência.

### Validação no formulário

- Nome, instituição, data inicial e data final são obrigatórios.
- A data final deve ser maior ou igual à data inicial. O seletor da data final já bloqueia dias anteriores à data inicial.
- A data inicial não pode ser anterior a hoje: o calendário desabilita esses dias, tanto no cadastro quanto na edição. Ao editar um evento já em andamento, a data inicial original continua aceita se não for alterada.
- A validação no formulário serve para dar retorno imediato. A validação que vale é a do backend, e seus erros são exibidos nos campos.

### Tratamento de erros da API

O `client.ts` converte as respostas de erro em um `ApiError`, que carrega a mensagem geral e os erros por campo. As páginas usam isso para mostrar cada mensagem junto ao campo certo.

### Estado e carregamento

O estado é mantido nos próprios componentes, com `useState` e `useEffect`, sem biblioteca de gerenciamento de estado. Isso é suficiente para o tamanho da aplicação. Ao trocar de aba, a página é recriada e a lista é buscada de novo, mantendo os dados atualizados.
