package com.zoopick.server.websocket.message;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record ChatErrorPayload(Reason reason, String message) implements ChatEventPayload {
    @Override
    public ChatEventMessage.Type type() {
        return ChatEventMessage.Type.ERROR;
    }

    public enum Reason {
        NOT_PERMITTED,
        NOT_FOUND,
        BAD_REQUEST,
        INTERNAL_SERVER_ERROR,
    }
}
