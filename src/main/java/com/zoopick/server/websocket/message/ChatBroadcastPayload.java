package com.zoopick.server.websocket.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ChatBroadcastPayload(
        @JsonProperty("sender_nickname") String senderNickname,
        String message
) implements ChatEventPayload {
    @Override
    public ChatEventMessage.Type type() {
        return ChatEventMessage.Type.MESSAGE;
    }
}
