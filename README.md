# 🍽️ EasyTable API

API para gerenciamento de comandas, pedidos e operações da cozinha em estabelecimentos como bares e restaurantes.

---

## 🚀 Endpoints

---

### 🔖 Comandas

`Base URL: /comandas/v1`

#### 📌 Fechar Comanda
Fecha a comanda e retorna o total da conta.

- **POST** `/comandas/v1/{codigoQR}/fechar`
- **Parâmetros de caminho:**
  - `codigoQR`: código QR da comanda
- **Resposta:** `FechamentoResponse` com ID da comanda, valor total e mensagem de sucesso

#### ♻️ Reabrir Comanda
Reabre uma comanda previamente fechada.

- **PATCH** `/comandas/v1/reabrir/{codigoQR}`
- **Parâmetros de caminho:**
  - `codigoQR`: código QR da comanda
- **Resposta:** `ComandaResponse` com os dados da comanda reaberta

---

### 📦 Pedidos

`Base URL: /pedidos/v1`

#### ➕ Criar Pedido
Cria um novo pedido vinculado a uma comanda.

- **POST** `/pedidos/v1/create`
- **Body (JSON):** `CreatePedidoCommand`
- **Resposta:** `CreatePedidoResponse` com os detalhes do pedido criado

#### 🔄 Atualizar Status do Pedido
Atualiza o status de um pedido enviando um evento (ex: `INICIAR_PREPARO`, `MARCAR_PRONTO`, `ENTREGAR`). O sistema utiliza uma State Machine para validar a transição.

- **PATCH** `/pedidos/v1/{id}/status`
- **Parâmetros de caminho:**
  - `id`: ID do pedido
- **Query param:** `evento` (ex: `INICIAR_PREPARO`, `MARCAR_PRONTO`, `ENTREGAR`, `CANCELAR`)
- **Resposta:** `UpdateStatusPedidoResponse` com status atualizado (baseado na transição da State Machine)

---

### ⚙️ State Machine (Fluxo de Pedidos)

O sistema utiliza **Spring State Machine** para garantir a consistência dos estados dos pedidos. O status de um pedido não é alterado livremente, mas sim através de **eventos** que disparam transições permitidas.

#### Estados Disponíveis:
- `PENDENTE`: Estado inicial após criação.
- `EM_PREPARACAO`: Pedido sendo preparado na cozinha.
- `PRONTO`: Preparação finalizada.
- `ENTREGUE`: Pedido entregue ao cliente na mesa.
- `PAGO`: Pagamento confirmado e pedido encerrado.
- `CANCELADO`: Pedido cancelado (possível a partir de PENDENTE, EM_PREPARACAO ou PRONTO).

#### Eventos e Transições:
| Evento | Origem | Destino |
| :--- | :--- | :--- |
| `INICIAR_PREPARO` | `PENDENTE` | `EM_PREPARACAO` |
| `MARCAR_PRONTO` | `EM_PREPARACAO` | `PRONTO` |
| `ENTREGAR` | `PRONTO` | `ENTREGUE` |
| `CONFIRMAR_PAGAMENTO` | `ENTREGUE` | `PAGO` |
| `CANCELAR` | `PENDENTE`, `EM_PREPARACAO`, `PRONTO` | `CANCELADO` |

---

### 🍳 Cozinha

`Base URL: /cozinha/v1`

#### 📋 Listar Pedidos
Lista todos os pedidos pendentes ou em preparo (normalmente consumidos pela tela da cozinha).

- **GET** `/cozinha/v1`
- **Resposta:** `ListaPedidosResponse` com lista de pedidos

---

### 🗂️ Outros Endpoints

Para mais detalhes, consulte a documentação Swagger.

---

## 💡 Tecnologias

- Java 21+
- Spring Boot 3
- Spring Web / Validation
- Spring State Machine
- Redis
- Kafka
- WebSocket (STOMP)
- MongoDB
- OpenAPI 3 / Swagger
- Docker

---

## 🔐 Segurança

- Autenticação ainda não implementada (em desenvolvimento).
- Futuro: integração com Keycloak ou JWT.

---

## 📡 WebSocket

- **Endpoint WebSocket:** `/pedidos/v1/ws` (Suporta SockJS)
- **Tópicos:**
  - `/topic/pedidos`: Receber novos pedidos ou atualizações em tempo real
  - `/topic/pedidos/remover`: Receber ID do pedido para remoção da tela da cozinha (quando entregue)

---

## 📦 Estrutura do Projeto

├── controllers/
│ ├── CozinhaController.java
│ ├── PedidoController.java
│ └── ComandaController.java
├── handlers/
├── services/
├── repositories/
├── borders/
│ ├── dtos/
│ ├── entities/
│ └── handlers/
└── shared/

---

## 📄 Documentação Swagger

Acesse a documentação interativa em:
http://localhost:8080/swagger-ui.html

---

## 🧪 Exemplos de Teste

### Criar Pedido
POST /pedidos/v1/create
```json
{
  "mesaId": "MESA_01",
  "comandaId": "xyz789",
  "itensIds": ["item1", "item2"]
}
```

### Atualizar Status
PATCH /pedidos/v1/28e6bf1a/status?evento=MARCAR_PRONTO

---

👨‍💻 Contribuição
Pull Requests são bem-vindos! Crie uma branch com o nome do recurso, siga o padrão de código e escreva testes sempre que possível.
