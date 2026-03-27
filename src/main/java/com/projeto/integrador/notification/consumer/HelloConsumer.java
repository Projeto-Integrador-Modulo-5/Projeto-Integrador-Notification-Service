package com.projeto.integrador.notification.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class HelloConsumer {

    private static final Logger log = LoggerFactory.getLogger(HelloConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public HelloConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "hello.world", groupId = "notification-group")
    public void consume(String message) {
        log.info("Mensagem recebida do Kafka: {}", message);

        // Envia para todos os clientes conectados no canal /topic/hello
        messagingTemplate.convertAndSend("/topic/hello", message);

        log.info("Mensagem enviada via WebSocket para /topic/hello");
    }
}
