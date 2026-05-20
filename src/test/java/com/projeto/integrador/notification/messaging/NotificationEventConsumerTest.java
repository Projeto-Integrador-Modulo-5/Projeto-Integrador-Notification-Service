package com.projeto.integrador.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock private SimpMessagingTemplate messagingTemplate;

    private NotificationEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        consumer = new NotificationEventConsumer(messagingTemplate);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ── mensagem válida ───────────────────────────────────────────────────────

    @Test
    void consume_shouldSendWebSocketPayloadForValidEvent() throws Exception {
        UUID userId  = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        NotificationSendEvent event = new NotificationSendEvent(
            userId, orderId, "ORDER_CREATED", "Seu pedido foi criado!");

        String json = objectMapper.writeValueAsString(event);
        consumer.consume(json);

        ArgumentCaptor<WebSocketPayload> captor = ArgumentCaptor.forClass(WebSocketPayload.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/notifications/" + userId), captor.capture());

        WebSocketPayload payload = captor.getValue();
        assertThat(payload.orderId()).isEqualTo(orderId);
        assertThat(payload.type()).isEqualTo("ORDER_CREATED");
        assertThat(payload.message()).isEqualTo("Seu pedido foi criado!");
        assertThat(payload.trackingCode()).isNull(); // notification.send não inclui tracking
    }

    @Test
    void consume_shouldSendToCorrectUserDestination() throws Exception {
        UUID userId  = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        NotificationSendEvent event = new NotificationSendEvent(
            userId, orderId, "ORDER_SHIPPED", "Pedido enviado!");

        String json = objectMapper.writeValueAsString(event);
        consumer.consume(json);

        verify(messagingTemplate).convertAndSend(
            eq("/topic/notifications/" + userId), any(WebSocketPayload.class));
    }

    // ── JSON inválido ─────────────────────────────────────────────────────────

    @Test
    void consume_shouldNotThrowForInvalidJson() {
        // Deve apenas logar o erro e não propagar exceção
        consumer.consume("not valid json at all");

        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void consume_shouldNotThrowForEmptyMessage() {
        // JSON vazio deserializa para um evento com campos null — não deve lançar exceção
        // O consumer vai tentar enviar para "/topic/notifications/null" mas sem explodir
        consumer.consume("{}");
        // Verifica apenas que nenhuma exceção foi lançada (sem assertion sobre o mock)
    }
}
