package com.zoopick.server.websocket;

import com.zoopick.server.websocket.message.ChatInformationPayload;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NullMarked
public class WebSocketSessionManager {
    private final Map<Long, Set<WebSocketSession>> sessionByRoom = new ConcurrentHashMap<>();
    private final Map<String, Long> roomBySessionId = new ConcurrentHashMap<>();
    private final ChatEventSender chatEventSender;

    public WebSocketSessionManager(ChatEventSender chatEventSender) {
        this.chatEventSender = chatEventSender;
    }

    public void join(long roomId, WebSocketSession session) {
        Long previousRoomId = roomBySessionId.put(session.getId(), roomId);
        if (previousRoomId != null && previousRoomId != roomId)
            leave(previousRoomId, session);
        sessionByRoom.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet())
                .add(session);
        chatEventSender.sendMessageSafely(session, new ChatInformationPayload("환영합니다."));
    }

    public void leave(WebSocketSession session) {
        Long roomId = roomBySessionId.remove(session.getId());
        if (roomId != null)
            leave(roomId, session);
    }

    private void leave(long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionByRoom.get(roomId);
        if (sessions == null)
            return;

        sessions.remove(session);
        if (sessions.isEmpty())
            sessionByRoom.remove(roomId);
    }

    public Set<WebSocketSession> getSessionsByRoom(long roomId) {
        return sessionByRoom.getOrDefault(roomId, Set.of());
    }
}
