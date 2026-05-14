package com.zoopick.server.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.zoopick.server.dto.notification.ChangeReadStatusResult;
import com.zoopick.server.entity.NotificationType;
import com.zoopick.server.entity.User;
import com.zoopick.server.entity.ZoopickNotification;
import com.zoopick.server.exception.DataNotFoundException;
import com.zoopick.server.mapper.notification.NotificationMapper;
import com.zoopick.server.repository.NotificationRepository;
import com.zoopick.server.repository.UserRepository;
import com.zoopick.server.service.notification.payload.NotificationPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    @DisplayName("FCM 토큰 등록 - 성공")
    void register_Success() {
        // given
        long userId = 1L;
        String fcmToken = "test_fcm_token";
        User user = User.builder().id(userId).build();

        given(userRepository.findByIdOrThrow(userId)).willReturn(user);

        // when
        notificationService.register(userId, fcmToken);

        // then
        assertThat(user.getFcmToken()).isEqualTo(fcmToken);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("알림 전송 - 성공 (FirebaseStaticMock 포함)")
    void send_Success() throws Exception {
        // given
        User user = User.builder().id(1L).fcmToken("valid_token").build();

        NotificationPayload payloadMock = mock(NotificationPayload.class);
        given(payloadMock.toMap()).willReturn(Map.of());

        given(payloadMock.type()).willReturn(NotificationType.values()[0]);

        SendNotificationCommand command = new SendNotificationCommand("제목", "내용", payloadMock);
        ZoopickNotification notification = ZoopickNotification.builder().id(100L).user(user).build();

        given(notificationMapper.toZoopickNotification(user, command)).willReturn(notification);
        given(notificationRepository.save(any(ZoopickNotification.class))).willReturn(notification);

        // static mocking for FirebaseMessaging
        try (MockedStatic<FirebaseMessaging> mockedFirebase = mockStatic(FirebaseMessaging.class)) {
            FirebaseMessaging firebaseMessagingMock = mock(FirebaseMessaging.class);
            mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessagingMock);
            given(firebaseMessagingMock.send(any(Message.class))).willReturn("message_id_123");

            // when
            String result = notificationService.send(user, command);

            // then
            assertThat(result).isEqualTo("message_id_123");
            verify(notificationRepository).save(any());
            verify(firebaseMessagingMock).send(any(Message.class));
        }
    }

    @Test
    @DisplayName("알림 전송 실패 - FCM 토큰이 없는 경우 DataNotFoundException 발생")
    void send_Fail_NoFcmToken() {
        // given
        User user = User.builder().id(1L).schoolEmail("test@mju.ac.kr").fcmToken(null).build();
        SendNotificationCommand command = mock(SendNotificationCommand.class);

        // when & then
        assertThatThrownBy(() -> notificationService.send(user, command))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessageContaining("FCM 토큰");
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void markAsRead_Success() {
        // given
        long userId = 1L;
        User user = User.builder().id(userId).build();
        ZoopickNotification notification1 = ZoopickNotification.builder().id(10L).user(user).build();
        ZoopickNotification notification2 = ZoopickNotification.builder().id(11L).user(user).build();

        List<Long> notificationIds = List.of(10L, 11L);
        given(notificationRepository.findAllById(notificationIds)).willReturn(List.of(notification1, notification2));

        // when
        ChangeReadStatusResult result = notificationService.markAsRead(userId, notificationIds);

        // then
        assertThat(result.getSucceedIds()).containsExactly(10L, 11L);
        assertThat(notification1.getReadAt()).isNotNull();
        assertThat(notification2.getReadAt()).isNotNull();
        verify(notificationRepository).saveAll(anyList());
    }
}
