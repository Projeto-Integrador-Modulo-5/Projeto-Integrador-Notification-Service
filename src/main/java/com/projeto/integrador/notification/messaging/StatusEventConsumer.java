package com.projeto.integrador.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StatusEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(StatusEventConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public StatusEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "order.status.updated", groupId = "notification-group")
    public void consume(String message) {
        try {
            OrderStatusUpdatedEvent event = objectMapper.readValue(message, OrderStatusUpdatedEvent.class);
            log.info("Recebido order.status.updated: orderId={}, status={}", event.orderId(), event.newStatus());

            String notificationMessage = buildMessage(event.newStatus(), event.orderId().toString());
            String type = "ORDER_" + event.newStatus();

            WebSocketPayload payload = new WebSocketPayload(
                event.orderId(),
                type,
                notificationMessage,
                event.trackingCode(),
                LocalDateTime.now()
            );

            String destination = "/topic/notifications/" + event.userId();
            messagingTemplate.convertAndSend(destination, payload);
            log.info("WebSocket enviado para {} → {}", destination, type);

        } catch (Exception e) {
            log.error("Erro ao processar order.status.updated: {}", e.getMessage(), e);
        }
    }

    private String buildMessage(String status, String orderId) {
        String shortId = orderId.substring(0, 8).toUpperCase();
        return switch (status) {
            case "SHIPPED" -> "Seu pedido #" + shortId + " foi enviado! Rastreie pelo código " + status;
            case "DELIVERED" -> "Seu pedido #" + shortId + " foi entregue!";
            default -> "Status do seu pedido #" + shortId + " atualizado para " + status;
        };
    }
}
