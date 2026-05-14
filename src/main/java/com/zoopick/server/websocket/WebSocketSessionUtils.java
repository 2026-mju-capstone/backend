package com.zoopick.server.websocket;

import com.zoopick.server.exception.InternalServerException;
import org.springframework.web.socket.WebSocketSession;

public final class WebSocketSessionUtils {
    public static long getUserId(WebSocketSession session) {
        return getAttribute(session, AuthHandshakeInterceptor.USER_ID_ATTRIBUTE, Long.class);
    }

    public static String getNickname(WebSocketSession session) {
        return getAttribute(session, AuthHandshakeInterceptor.USER_NICKNAME_ATTRIBUTE, String.class);
    }

    private static <T> T getAttribute(WebSocketSession senderSession, String attributeName, Class<T> type) {
        Object attributeValue = senderSession.getAttributes().get(attributeName);
        if (type.isInstance(attributeValue))
            return type.cast(attributeValue);
        throw new InternalServerException("Attribute " + attributeName + " is not a " + type);
    }
}
