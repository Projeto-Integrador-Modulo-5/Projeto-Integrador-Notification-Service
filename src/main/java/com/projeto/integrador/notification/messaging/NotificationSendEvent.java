package com.projeto.integrador.notification.messaging;

import java.util.UUID;

public record NotificationSendEvent(UUID userId, UUID orderId, String type, String message) {}
