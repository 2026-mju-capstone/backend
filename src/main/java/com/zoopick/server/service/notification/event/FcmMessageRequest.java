package com.zoopick.server.service.notification.event;

import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Optional;

@NullMarked
public record FcmMessageRequest(
        Optional<String> token,
        String title,
        String body,
        Map<String, String> data
) {
    public boolean hasToken() {
        return token.isPresent();
    }
}
