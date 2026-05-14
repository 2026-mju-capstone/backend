package com.zoopick.server.websocket.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ChatEventPayload {
    @JsonIgnore
    ChatEventMessage.Type type();
}
