package com.zoopick.server.service;

import com.zoopick.server.dto.match.CreateCctvMatchEvent;
import com.zoopick.server.entity.*;
import com.zoopick.server.service.notification.NotificationService;
import com.zoopick.server.service.notification.SendNotificationCommand;
import com.zoopick.server.service.notification.payload.CctvFoundPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateCctvMatchEventListner {
    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchCreated(CreateCctvMatchEvent event) {
        Room room = event.room();
        Item item = event.item();
        CctvDetectionMatch cctvDetectionMatch = event.cctvDetectionMatch();
        String title = event.itemPost().getTitle();

        notificationService.send(event.item().getReporter(), new SendNotificationCommand(
                "도난 의심",
                "회원님이 등록한 %s와 유사한 물건이 %s에서 도난이 의심돼요.".formatted(title, room.getName()),
                CctvFoundPayload.of(item, cctvDetectionMatch)));
        log.info("FCM 전송 성공 matchId: {}", cctvDetectionMatch.getId());
    }
}
