package com.zoopick.server.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.zoopick.server.exception.DataNotFoundException;
import com.zoopick.server.service.notification.event.FcmMessageRequest;
import com.zoopick.server.service.notification.event.NotificationDispatchRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@NullMarked
public class NotificationDispatchEventListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationDispatchRequestedEvent event) throws FirebaseMessagingException {
        List<FcmMessageRequest> messageRequests = event.messages();
        List<Message> messages = messageRequests.stream()
                .filter(FcmMessageRequest::hasToken)
                .map(this::toMessage)
                .toList();

        if (!messages.isEmpty())
            FirebaseMessaging.getInstance().sendEach(messages);
        int unspentCount = messageRequests.size() - messages.size();
        if (unspentCount > 0)
            log.warn("{} / {} notifications could not be dispatched", unspentCount, messageRequests.size());
    }

    private Message toMessage(FcmMessageRequest request) {
        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();
        return Message.builder()
                .setNotification(notification)
                .putAllData(request.data())
                .setToken(request.token().orElseThrow(() -> DataNotFoundException.from("FCM 토큰", request)))
                .build();
    }
}
