# Projeto-Integrador-Notification-Service

Microsserviço de notificações do e-commerce de camisetas — Projeto Integrador ADS 4º Período · PUC Goiás · 2026/1.

Responsável por consumir eventos de status do Apache Kafka e entregar atualizações em tempo real ao frontend via WebSocket/STOMP, além de enviar e-mails transacionais via SMTP.

---

## Responsabilidades

- Consumir eventos do tópico `notifications-topic`
- Empurrar atualizações de status ao frontend via WebSocket/STOMP (`/topic/orders/{id}`)
- Enviar e-mails transacionais ao cliente (confirmação, envio, entrega)

---

## Fluxo de eventos

```
Logistics Service
  └─► [notifications-topic] ──► Notification Service
                                      ├─► WebSocket/STOMP ──► Frontend (tempo real)
                                      └─► SMTP ──► E-mail do cliente
```

---

## Stack

| Camada      | Tecnologia                                              |
|-------------|---------------------------------------------------------|
| Linguagem   | Java 21                                                 |
| Framework   | Spring Boot 3.x (WebSocket, Kafka, Mail)                |
| Mensageria  | Apache Kafka — consumer group `notification-group`      |
| WebSocket   | Spring WebSocket + STOMP                                |
| E-mail      | SMTP (ex: SendGrid)                                     |
| Build       | Maven (Wrapper incluído)                                |
| Container   | Docker (orquestrado via `Projeto-Integrador-Infra`)     |

---

## Estrutura de pacotes

```
com.ecommerce.notification/
├── controller/   # Endpoint WebSocket (handshake)
├── service/      # Lógica de envio (WebSocket + e-mail)
├── messaging/    # @KafkaListener — consumer de notifications-topic
├── domain/       # DTOs de evento — zero framework
└── dto/          # Java Records (OrderEvent)
```

---

## WebSocket — como funciona

O frontend conecta no endpoint `/ws` usando SockJS + STOMP e se inscreve no destino `/topic/orders/{orderId}`. Quando o Notification Service consome um evento do Kafka, faz `convertAndSend` para esse destino — o browser recebe a atualização **sem recarregar a página**.

```
Browser ──[SockJS/STOMP]──► /ws
  └─► subscribe: /topic/orders/{id}
          ↑
  Notification Service ──[STOMP push]──► atualização de status
```

---

## Configuração Kafka

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    consumer:
      group-id: notification-group
      auto-offset-reset: earliest
      value-deserializer: StringDeserializer
```

---

## Configuração

```bash
cp .env.example .env
# configure KAFKA_BOOTSTRAP_SERVERS, SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS
```

---

## Executando localmente

> Recomendado subir a infraestrutura pelo repositório `Projeto-Integrador-Infra` antes.

```bash
./mvnw spring-boot:run
```

A aplicação sobe na porta `8082`. O endpoint WebSocket estará disponível em `http://localhost:8082/ws`.

---

## Testes

```bash
./mvnw verify
```

---

## Repositórios relacionados

| Repositório | Responsabilidade |
|---|---|
| [Projeto-Integrador-Infra](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Infra) | Docker Compose e infraestrutura |
| [Projeto-Integrador-Backend](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Backend) | API REST principal |
| [Projeto-Integrador-Logistics-Service](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Logistics-Service) | Publica eventos em `notifications-topic` |
| [Projeto-Integrador-Frontend](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Frontend) | Recebe atualizações via WebSocket/STOMP |
