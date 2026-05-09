package com.zoopick.server.service.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zoopick.server.dto.notification.SendNotificationRequest;
import com.zoopick.server.entity.NotificationType;
import com.zoopick.server.mapper.SendNotificationRequestMapper;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * <p>
 * 알림 전송과 데이터베이스 저장에 사용될 payload의 인터페이스
 * </p>
 * <ul>
 * <li>Firebase의 {@link com.google.firebase.messaging.Message}에서 데이터의 key, value 모두 String으로 받기 때문에
 * 알림으로 보내는 데이터는 {@code Map<String, String>}형태를 사용합니다.</li>
 * <li><b>실제 데이터베이스에 저장될 때는 원본 타입이 보존됩니다.</b></li>
 * </ul>
 *
 * @see SendNotificationRequestMapper#toCommand(SendNotificationRequest)
 * @see SendNotificationCommand
 */
@NullMarked
public interface NotificationPayload {
    @JsonIgnore
    NotificationType type();

    /**
     * Firebase의 {@link com.google.firebase.messaging.Message}에서 데이터의 key, value 모두 String으로 받기 때문에
     * <b>알림으로 보내는 데이터는 {@code Map<String, String>}형태를 사용합니다.</b>
     *
     * @return {@code Map<String, String>}의 payload 데이터
     */
    @JsonIgnore
    Map<String, String> toMap();
}
