# Roteiro de Implementação: Entradas, Saídas e Relatórios

Este roteiro detalha os passos necessários para implementar entradas e saídas de valores em contas, bem como a geração de relatórios. Utilize este documento para acompanhar o progresso, marcando o que está funcionando e o que ainda precisa ser implementado.

---

## 1. Backend

### a) Criar Entidades e Repositórios
- [ ] **Entidade `Transacao`**:
  - Local: `backend/src/main/java/com/gestaofinanceirapessoal/domains/`
  - Campos sugeridos:
    - `id` (Long, chave primária)
    - `tipo` (Enum: ENTRADA ou SAÍDA)
    - `valor` (BigDecimal)
    - `data` (LocalDateTime)
    - `descricao` (String)
    - `contaId` (Long, referência à conta associada)
- [ ] **Enum `TipoTransacao`**:
  - Local: `backend/src/main/java/com/gestaofinanceirapessoal/domains/enums/`
  - Valores: `ENTRADA`, `SAIDA`.
- [ ] **Repositório `TransacaoRepository`**:
  - Local: `backend/src/main/java/com/gestaofinanceirapessoal/repositories/`
  - Métodos:
    - `List<Transacao> findByContaId(Long contaId);`
    - `List<Transacao> findByContaIdAndTipo(Long contaId, TipoTransacao tipo);`

### b) Criar Serviço
- [ ] **Serviço `TransacaoService`**:
  - Local: `backend/src/main/java/com/gestaofinanceirapessoal/services/`
  - Métodos sugeridos:
    - `void registrarTransacao(Transacao transacao);`
    - `List<Transacao> listarTransacoes(Long contaId);`
    - `BigDecimal calcularSaldo(Long contaId);`

### c) Criar Controlador
- [ ] **Controlador `TransacaoController`**:
  - Local: `backend/src/main/java/com/gestaofinanceirapessoal/Main/`
  - Endpoints sugeridos:
    - `POST /transacoes`: Registrar uma transação.
    - `GET /transacoes/{contaId}`: Listar transações de uma conta.
    - `GET /transacoes/saldo/{contaId}`: Obter saldo da conta.

---

## 2. Frontend

### a) Criar Serviço Angular
- [ ] **Serviço `TransacaoService`**:
  - Local: `frontend/src/app/core/`
  - Métodos sugeridos:
    - `registrarTransacao(transacao: TransacaoDTO): Observable<any>;`
    - `listarTransacoes(contaId: number): Observable<Transacao[]>;`
    - `obterSaldo(contaId: number): Observable<number>;`
- [ ] **Modelo `TransacaoDTO`**:
  - Local: `frontend/src/app/core/`
  - Campos:
    - `id: number;`
    - `tipo: 'ENTRADA' | 'SAIDA';`
    - `valor: number;`
    - `data: string;`
    - `descricao: string;`
    - `contaId: number;`

### b) Criar Componentes
- [ ] **Componente `TransacaoForm`**:
  - Local: `frontend/src/app/features/contas/`
  - Funcionalidade: Formulário para registrar entradas e saídas.
  - Campos:
    - Tipo (radio button: ENTRADA ou SAÍDA)
    - Valor (input numérico)
    - Descrição (input texto)
    - Data (input datetime)
- [ ] **Componente `TransacaoList`**:
  - Local: `frontend/src/app/features/contas/`
  - Funcionalidade: Listar transações de uma conta.
- [ ] **Componente `Relatorio`**:
  - Local: `frontend/src/app/features/contas/`
  - Funcionalidade: Exibir saldo total e gráficos (opcional).

### c) Atualizar Rotas
- [ ] Atualizar arquivo `frontend/src/app/app.routes.ts`:
  ```typescript
  { path: 'contas/:id/transacoes', component: TransacaoList },
  { path: 'contas/:id/relatorio', component: Relatorio },
  ```

---

## 3. Relatório

### Backend
- [ ] Endpoint: `GET /transacoes/relatorio/{contaId}`
- Retorno:
  ```json
  {
    "saldo": 1000.00,
    "entradas": 1500.00,
    "saidas": 500.00,
    "transacoes": [
      { "tipo": "ENTRADA", "valor": 1000.00, "data": "2025-10-25T10:00:00", "descricao": "Salário" },
      { "tipo": "SAIDA", "valor": 500.00, "data": "2025-10-26T15:00:00", "descricao": "Aluguel" }
    ]
  }
  ```

### Frontend
- [ ] Exibir saldo total, entradas e saídas.
- [ ] Utilizar gráficos (ex.: `chart.js` ou `ng2-charts`) para visualização.

---

Marque as tarefas concluídas à medida que forem implementadas. Se precisar de ajuda em algum ponto, consulte este roteiro ou peça suporte!