package com.zoopick.server.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoopick.server.dto.notification.SendNotificationRequest;
import com.zoopick.server.entity.NotificationType;
import com.zoopick.server.exception.InternalServerException;
import com.zoopick.server.service.command.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@NullMarked
public class SendNotificationRequestMapper {
    private final Map<NotificationType, Class<? extends NotificationPayload>> payloadTypes = Map.of(
            NotificationType.CHAT_MESSAGE, ChatMessagePayload.class,
            NotificationType.ITEM_RETURNED, ItemReturnedPayload.class,
            NotificationType.LOCKER_READY, LockerReadyPayload.class,
            NotificationType.THEFT_SUSPECTED, TheftSuspectedPayload.class,
            NotificationType.MATCH_FOUND, MatchFoundPayload.class
    );

    private final ObjectMapper objectMapper;

    public SendNotificationCommand toCommand(SendNotificationRequest request) {
        try {
            NotificationType type = request.getType();
            if (!payloadTypes.containsKey(type))
                throw new UnsupportedOperationException("Unsupported notification type : " + type);
            Class<? extends NotificationPayload> payloadType = payloadTypes.get(type);
            NotificationPayload payload = objectMapper.convertValue(request.getPayload(), payloadType);
            return new SendNotificationCommand(request.getTitle(), request.getBody(), payload);
        } catch (RuntimeException exception) {
            throw new InternalServerException("Failed to convert payload " + request.getPayload(), exception);
        }
    }
}
