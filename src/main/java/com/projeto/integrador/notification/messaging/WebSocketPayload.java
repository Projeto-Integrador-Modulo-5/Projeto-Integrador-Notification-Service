package com.projeto.integrador.notification.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record WebSocketPayload(
    UUID orderId,
    String type,
    String message,
    String trackingCode,
    LocalDateTime sentAt
) {}
