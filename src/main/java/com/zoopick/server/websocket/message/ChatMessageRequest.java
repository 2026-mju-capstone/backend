package com.zoopick.server.websocket.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatMessageRequest(Type type, @JsonProperty("room_id") long roomId, String message) {
    public enum Type {
        JOIN,
        MESSAGE
    }
}
