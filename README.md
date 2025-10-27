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
Atualiza o status de um pedido (por exemplo: `PENDENTE`, `EM_PREPARACAO`, `PRONTO`, `ENTREGUE`).

- **PATCH** `/pedidos/v1/{id}/status`
- **Parâmetros de caminho:**
  - `id`: ID do pedido
- **Query param:** `status` (ex: `PRONTO`)
- **Resposta:** `UpdateStatusPedidoResponse` com status atualizado

---

### 🍳 Cozinha

`Base URL: /cozinha/v1`

#### 📋 Listar Pedidos
Lista todos os pedidos pendentes ou em preparo (normalmente consumidos pela tela da cozinha).

- **GET** `/cozinha/v1`
- **Resposta:** `ListaPedidosResponse` com lista de pedidos

---

## 💡 Tecnologias

- Java 21+
- Spring Boot 3
- Spring Web / Validation
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

- **Endpoint WebSocket:** `/pedidos/v1/ws`
- **Tópicos:**
  - `/topic/pedidos`: Receber pedidos em tempo real
  - `/topic/pedidos/remover`: Remoção de pedidos entregues

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

```json
POST /pedidos/v1/create
{
  "mesaId": "abc123",
  "comandaId": "xyz789",
  "itensIds": ["item1", "item2"]
}
```
Atualizar Status
PATCH /pedidos/v1/28e6bf1a/status?status=PRONTO

---

👨‍💻 Contribuição
Pull Requests são bem-vindos! Crie uma branch com o nome do recurso, siga o padrão de código e escreva testes sempre que possível.
