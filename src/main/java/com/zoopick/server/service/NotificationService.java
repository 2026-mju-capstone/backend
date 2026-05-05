package com.zoopick.server.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.zoopick.server.dto.notification.ChangeReadStatusResult;
import com.zoopick.server.dto.notification.NotificationRecord;
import com.zoopick.server.dto.notification.SendNotificationRequest;
import com.zoopick.server.entity.User;
import com.zoopick.server.entity.ZoopickNotification;
import com.zoopick.server.exception.AccessTokenException;
import com.zoopick.server.exception.BadRequestException;
import com.zoopick.server.exception.DataNotFoundException;
import com.zoopick.server.mapper.NotificationMapper;
import com.zoopick.server.repository.NotificationRepository;
import com.zoopick.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@NullMarked
public class NotificationService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public void register(String email, String fcmToken) throws AccessTokenException, DataNotFoundException {
        User user = userRepository.findBySchoolEmail(email)
                .orElseThrow(() -> DataNotFoundException.from("사용자", email));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    public String send(long userId, SendNotificationRequest request) throws FirebaseMessagingException {
        User user = userRepository.findByIdOrThrow(userId);
        return this.send(user, request);
    }

    public String send(User user, SendNotificationRequest request) throws FirebaseMessagingException {
        String fcmToken = user.getFcmToken();
        if (fcmToken == null)
            throw DataNotFoundException.from("FCM 토큰", user.getSchoolEmail());
        request.getType().validatePayload(request.getPayload());

        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();
        Message message = Message.builder()
                .setNotification(notification)
                .putAllData(request.getPayload())
                .setToken(fcmToken)
                .build();

        ZoopickNotification zoopickNotification = notificationMapper.toZoopickNotification(user, request);
        notificationRepository.save(zoopickNotification);
        return FirebaseMessaging.getInstance().send(message);
    }

    public String broadcast(SendNotificationRequest request) throws FirebaseMessagingException {
        request.getType().validatePayload(request.getPayload());

        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        List<User> users = userRepository.findAll();
        List<ZoopickNotification> zoopickNotifications = users.stream()
                .map(user -> notificationMapper.toZoopickNotification(user, request))
                .toList();
        notificationRepository.saveAll(zoopickNotifications);

        List<Message> messages = users.stream()
                .map(User::getFcmToken)
                .filter(Objects::nonNull)
                .map(fcmToken -> Message.builder()
                        .setNotification(notification)
                        .putAllData(request.getPayload())
                        .setToken(fcmToken)
                        .build())
                .toList();

        return FirebaseMessaging.getInstance().sendEach(messages).toString();
    }

    public List<NotificationRecord> getNotifications(String email) {
        return findNotificationsWith(email, notificationRepository::findByUserIdOrderByCreatedAtDesc);
    }

    public List<NotificationRecord> getUnreadNotifications(String email) {
        return findNotificationsWith(email, notificationRepository::findByUserIdAndReadAtIsNullOrderByCreatedAtDesc);
    }

    private List<NotificationRecord> findNotificationsWith(String email, Function<Long, List<ZoopickNotification>> repositoryAccessor) {
        User user = userRepository.findBySchoolEmail(email)
                .orElseThrow(() -> DataNotFoundException.from("사용자", email));

        List<ZoopickNotification> notifications = repositoryAccessor.apply(user.getId());
        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    public ChangeReadStatusResult markAsRead(String email, List<Long> notificationIds) {
        return changeReadStatusRequest(email, notificationIds, notification -> notification.setReadAt(LocalDateTime.now()));
    }

    public ChangeReadStatusResult markAsUnread(String email, List<Long> notificationIds) {
        return changeReadStatusRequest(email, notificationIds, notification -> notification.setReadAt(null));
    }

    private ChangeReadStatusResult changeReadStatusRequest(String email, List<Long> notificationIds, Consumer<ZoopickNotification> readStatusChangeAction) {
        List<ZoopickNotification> notifications = notificationRepository.findAllById(notificationIds);
        if (notifications.isEmpty())
            throw DataNotFoundException.from("알림", notificationIds);

        List<Long> succeedIds = new ArrayList<>();
        for (ZoopickNotification notification : notifications) {
            if (notification.getUser().getSchoolEmail().equals(email)) {
                readStatusChangeAction.accept(notification);
                succeedIds.add(notification.getId());
            }
        }
        if (succeedIds.isEmpty())
            throw new BadRequestException("현재 사용자의 알림이 아닙니다.");

        notificationRepository.saveAll(notifications);
        return new ChangeReadStatusResult(succeedIds);
    }
}
