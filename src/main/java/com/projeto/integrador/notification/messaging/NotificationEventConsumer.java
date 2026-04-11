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
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "notification.send", groupId = "notification-group")
    public void consume(String message) {
        try {
            NotificationSendEvent event = objectMapper.readValue(message, NotificationSendEvent.class);
            log.info("Recebido notification.send: userId={}, type={}", event.userId(), event.type());

            WebSocketPayload payload = new WebSocketPayload(
                event.orderId(),
                event.type(),
                event.message(),
                null,
                LocalDateTime.now()
            );

            String destination = "/topic/notifications/" + event.userId();
            messagingTemplate.convertAndSend(destination, payload);
            log.info("WebSocket enviado para {} → {}", destination, event.type());

        } catch (Exception e) {
            log.error("Erro ao processar notification.send: {}", e.getMessage(), e);
        }
    }
}
