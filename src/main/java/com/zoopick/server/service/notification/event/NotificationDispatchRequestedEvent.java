package com.zoopick.server.service.notification.event;

import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public record NotificationDispatchRequestedEvent(List<FcmMessageRequest> messages) {
}
