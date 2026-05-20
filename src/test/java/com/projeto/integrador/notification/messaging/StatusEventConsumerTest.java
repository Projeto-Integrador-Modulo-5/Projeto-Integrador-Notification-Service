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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusEventConsumerTest {

    @Mock private SimpMessagingTemplate messagingTemplate;

    private StatusEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        consumer = new StatusEventConsumer(messagingTemplate);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ── SHIPPED ───────────────────────────────────────────────────────────────

    @Test
    void consume_shouldSendWebSocketPayloadForShippedStatus() throws Exception {
        UUID orderId = UUID.fromString("11111111-0000-0000-0000-000000000000");
        UUID userId  = UUID.randomUUID();
        OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
            orderId, userId, "SHIPPED", "BR123456789PT", LocalDateTime.now());

        String json = objectMapper.writeValueAsString(event);
        consumer.consume(json);

        ArgumentCaptor<WebSocketPayload> captor = ArgumentCaptor.forClass(WebSocketPayload.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/notifications/" + userId), captor.capture());

        WebSocketPayload payload = captor.getValue();
        assertThat(payload.type()).isEqualTo("ORDER_SHIPPED");
        assertThat(payload.orderId()).isEqualTo(orderId);
        assertThat(payload.trackingCode()).isEqualTo("BR123456789PT");
        assertThat(payload.message()).contains("enviado");
    }

    // ── DELIVERED ─────────────────────────────────────────────────────────────

    @Test
    void consume_shouldSendWebSocketPayloadForDeliveredStatus() throws Exception {
        UUID orderId = UUID.fromString("22222222-0000-0000-0000-000000000000");
        UUID userId  = UUID.randomUUID();
        OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
            orderId, userId, "DELIVERED", null, LocalDateTime.now());

        String json = objectMapper.writeValueAsString(event);
        consumer.consume(json);

        ArgumentCaptor<WebSocketPayload> captor = ArgumentCaptor.forClass(WebSocketPayload.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/notifications/" + userId), captor.capture());

        WebSocketPayload payload = captor.getValue();
        assertThat(payload.type()).isEqualTo("ORDER_DELIVERED");
        assertThat(payload.message()).contains("entregue");
    }

    // ── status desconhecido ───────────────────────────────────────────────────

    @Test
    void consume_shouldSendGenericMessageForUnknownStatus() throws Exception {
        UUID orderId = UUID.fromString("33333333-0000-0000-0000-000000000000");
        UUID userId  = UUID.randomUUID();
        OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
            orderId, userId, "CANCELLED", null, LocalDateTime.now());

        String json = objectMapper.writeValueAsString(event);
        consumer.consume(json);

        ArgumentCaptor<WebSocketPayload> captor = ArgumentCaptor.forClass(WebSocketPayload.class);
        verify(messagingTemplate).convertAndSend(any(String.class), captor.capture());

        WebSocketPayload payload = captor.getValue();
        assertThat(payload.type()).isEqualTo("ORDER_CANCELLED");
        assertThat(payload.message()).contains("atualizado");
    }

    // ── JSON inválido ─────────────────────────────────────────────────────────

    @Test
    void consume_shouldNotThrowForInvalidJson() {
        // Deve apenas logar o erro e não propagar exceção
        consumer.consume("{ json invalido }");

        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }
}
