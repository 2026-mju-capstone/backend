package com.zoopick.server.websocket.message;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record ChatInformationPayload(String message) implements ChatEventPayload {
    @Override
    public ChatEventMessage.Type type() {
        return ChatEventMessage.Type.INFO;
    }
}
