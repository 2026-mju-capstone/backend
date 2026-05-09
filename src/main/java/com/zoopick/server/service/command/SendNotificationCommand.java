package com.zoopick.server.service.command;

import com.zoopick.server.dto.notification.SendNotificationRequest;
import com.zoopick.server.mapper.SendNotificationRequestMapper;
import com.zoopick.server.service.NotificationService;
import org.jspecify.annotations.NullMarked;

/**
 * 알림 전송을 위해 사용되는 Command
 *
 * @param title   알림 제목
 * @param body    알림 내용
 * @param payload {@link NotificationPayload}
 * @see SendNotificationRequestMapper#toCommand(SendNotificationRequest)
 * @see NotificationService
 */
@NullMarked
public record SendNotificationCommand(String title, String body, NotificationPayload payload) {

}
