package com.zoopick.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoopick.server.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@NullMarked
public class ChatWebSocketBroadcaster {
    private final WebSocketSessionManager webSocketSessionManager;
    private final ObjectMapper objectMapper;
    private final ChatRoomService chatRoomService;

    public void broadcast(long roomId, WebSocketSession senderSession, String message) throws IOException {
        long senderId = WebSocketSessionUtils.getUserId(senderSession);
        String senderNickname = WebSocketSessionUtils.getNickname(senderSession);
        ChatBroadcastMessage broadcastMessage = new ChatBroadcastMessage(senderNickname, message);
        String json = objectMapper.writeValueAsString(broadcastMessage);

        List<Long> receiverInWebSocketIds = new ArrayList<>();

        Set<WebSocketSession> sessions = webSocketSessionManager.getSessionsByRoom(roomId);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen() || session.getId().equals(senderSession.getId()))
                continue;

            long id = WebSocketSessionUtils.getUserId(session);
            receiverInWebSocketIds.add(id);
            session.sendMessage(new TextMessage(json));
        }

        for (long participantId : chatRoomService.getParticipants(roomId)) {
            if (senderId == participantId)
                continue;
            if (!receiverInWebSocketIds.contains(participantId))
                chatRoomService.sendMessageWithNotification(senderId, roomId, message);
        }
        receiverInWebSocketIds.forEach(receiverId -> chatRoomService.sendMessageWithoutNotification(senderId, roomId, message));
    }
}
