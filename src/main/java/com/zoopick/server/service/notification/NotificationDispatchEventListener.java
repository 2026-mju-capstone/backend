package com.zoopick.server.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.zoopick.server.exception.InternalServerException;
import com.zoopick.server.service.notification.event.FcmMessageRequest;
import com.zoopick.server.service.notification.event.NotificationDispatchRequestedEvent;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@NullMarked
public class NotificationDispatchEventListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationDispatchRequestedEvent event) {
        List<FcmMessageRequest> messages = event.messages();
        if (messages.isEmpty())
            return;

        try {
            FirebaseMessaging.getInstance().sendEach(messages.stream()
                    .map(this::toMessage)
                    .toList());
        } catch (FirebaseMessagingException exception) {
            throw new InternalServerException("Failed to dispatch FCM notifications: count=" + messages.size(), exception);
        }
    }

    private Message toMessage(FcmMessageRequest request) {
        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();
        return Message.builder()
                .setNotification(notification)
                .putAllData(request.data())
                .setToken(request.token())
                .build();
    }
}
