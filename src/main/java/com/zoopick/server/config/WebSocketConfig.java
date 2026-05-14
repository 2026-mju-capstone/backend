package com.zoopick.server.config;

import com.zoopick.server.websocket.AuthHandshakeInterceptor;
import com.zoopick.server.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@NullMarked
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(authHandshakeInterceptor);
    }
}
