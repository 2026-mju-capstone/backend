package com.zoopick.server.service.notification.event;

import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public record FcmMessageRequest(
        String token,
        String title,
        String body,
        Map<String, String> data
) {
}
