package com.zoopick.server.websocket;

import com.zoopick.server.repository.UserRepository;
import com.zoopick.server.security.JwtUtil;
import com.zoopick.server.security.UserPrincipal;
import com.zoopick.server.service.TokenValidationService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * 연결을 시도하는 사용자의 인증 정보를 확인하고 필요한 정보를 주입합니다.
 *
 * @see #USER_ID_ATTRIBUTE
 * @see #USER_NICKNAME_ATTRIBUTE
 */
@Component
@RequiredArgsConstructor
@NullMarked
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String USER_NICKNAME_ATTRIBUTE = "userNickname";
    public static final String USER_PRINCIPAL_ATTRIBUTE = "userPrincipal";

    private final JwtUtil jwtUtil;
    private final TokenValidationService tokenValidationService;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Optional<String> accessToken = extractAccessToken(request);
        if (accessToken.isEmpty() || !tokenValidationService.validateToken(accessToken.get())) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String email = jwtUtil.extractEmail(accessToken.get());
        return userRepository.findBySchoolEmail(email)
                .map(user -> {
                    UserPrincipal principal = new UserPrincipal(
                            user.getId(),
                            user.getSchoolEmail(),
                            user.getNickname(),
                            user.getRole()
                    );
                    attributes.put(USER_ID_ATTRIBUTE, user.getId());
                    attributes.put(USER_NICKNAME_ATTRIBUTE, user.getNickname());
                    attributes.put(USER_PRINCIPAL_ATTRIBUTE, principal);
                    return true;
                })
                .orElseGet(() -> {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                });
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            @Nullable Exception exception
    ) {
    }

    private Optional<String> extractAccessToken(ServerHttpRequest request) {
        String tokenFromQueryParam = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");
        if (tokenFromQueryParam != null && !tokenFromQueryParam.isBlank()) {
            return Optional.of(tokenFromQueryParam);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authorization.substring(7));
    }
}
