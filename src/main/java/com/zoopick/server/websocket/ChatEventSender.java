package com.zoopick.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoopick.server.websocket.message.ChatErrorPayload;
import com.zoopick.server.websocket.message.ChatEventMessage;
import com.zoopick.server.websocket.message.ChatEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@NullMarked
public class ChatEventSender {
    private final ObjectMapper objectMapper;

    /**
     * 메시지를 전송합니다.
     *
     * @param session 세션
     * @param payload 전송할 payload
     * @throws IOException 전송 실패 시
     */
    public void sendMessage(WebSocketSession session, ChatEventPayload payload) throws IOException {
        ChatEventMessage message = ChatEventMessage.of(payload);
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    /**
     * 예외 없이 메시지를 전송합니다. 예외가 발생하면 로그를 남깁니다.
     *
     * @param session 세션
     * @param payload 전송할 payload
     * @return 전송에 성공했을 시 true
     */
    public boolean sendMessageSafely(WebSocketSession session, ChatEventPayload payload) {
        try {
            sendMessage(session, payload);
            return true;
        } catch (IOException exception) {
            log.error("Failed to send websocket message. sessionId={}", session.getId(), exception);
            return false;
        }
    }

    /**
     * 예외 없이 오류 메시지를 전송합니다.예외가 발생하면 로그를 남깁니다.
     *
     * @param session 세션
     * @param reason  오류의 원인
     * @param message 메시지
     */
    public void sendErrorSafely(WebSocketSession session, ChatErrorPayload.Reason reason, String message) {
        sendMessageSafely(session, new ChatErrorPayload(reason, message));
    }
}
