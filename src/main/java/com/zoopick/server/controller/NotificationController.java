package com.zoopick.server.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.dto.notification.*;
import com.zoopick.server.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification API", description = "FCM 토큰을 등록 및 알림 전송")
@RestController
@RequiredArgsConstructor
@NullMarked
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "FCM 토큰 등록", description = "클라이언트의 FCM 토큰을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FCM 토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/api/auth/device-token")
    public ResponseEntity<CommonResponse<String>> registerFcmToken(
            Authentication authentication,
            @RequestBody @Valid FcmTokenRegistrationRequest request
    ) {
        String email = authentication.getName();
        notificationService.register(email, request.getToken());
        return ResponseEntity.ok(CommonResponse.success("FCM 토큰이 등록되었습니다."));
    }

    @Operation(summary = "대상에게 알림 전송", description = "클라이언트로 알림을 보냅니다. (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
            @ApiResponse(responseCode = "404", description = "닉네임을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "FirebaseMessaging 오류")
    })
    @PostMapping("/admin/notifications/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<String>> sendNotification(
            @PathVariable long userId,
            @RequestBody @Valid SendNotificationRequest request
    ) throws FirebaseMessagingException {
        String result = notificationService.send(userId, request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "모든 사용자에게 알림 전송", description = "모든 사용자에게 알림을 보냅니다. (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "FirebaseMessaging 오류")
    })
    @PostMapping("/admin/notifications/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<String>> broadcastNotification(
            @RequestBody @Valid SendNotificationRequest request
    ) throws FirebaseMessagingException {
        String result = notificationService.broadcast(request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "모든 알림 확인", description = "해당 사용자의 모든 알림을 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/api/notifications")
    public ResponseEntity<CommonResponse<List<NotificationRecord>>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        List<NotificationRecord> notificationRecords = notificationService.getNotifications(email);
        return ResponseEntity.ok(CommonResponse.success(notificationRecords));
    }

    @Operation(summary = "읽지 않은 알림 확인", description = "해당 사용자의 아직 읽지 않은 알림을 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/api/notifications/unread")
    public ResponseEntity<CommonResponse<List<NotificationRecord>>> getUnReadNotifications(Authentication authentication) {
        String email = authentication.getName();
        List<NotificationRecord> notificationRecords = notificationService.getUnreadNotifications(email);
        return ResponseEntity.ok(CommonResponse.success(notificationRecords));
    }

    @Operation(summary = "알림 읽음 처리", description = "해당 사용자의 여러 알림을 읽음 상태로 변경합니다. 상태 변경에 성공한 알림 번호를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "모든 알림이 사용자의 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 알림을 찾을 수 없음")
    })
    @PatchMapping("/api/notifications/mark-as-read")
    public ResponseEntity<CommonResponse<ChangeReadStatusResult>> markAsRead(Authentication authentication, @RequestBody @Valid ChangeReadStatusRequest request) {
        String email = authentication.getName();
        ChangeReadStatusResult result = notificationService.markAsRead(email, request.getNotificationIds());
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "알림 단건 읽음 처리", description = "해당 사용자의 특정 알림 하나를 읽음 상태로 변경합니다. 상태 변경에 성공한 알림 번호를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "알림이 사용자의 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 알림을 찾을 수 없음")
    })
    @PatchMapping("/api/notifications/{notificationId}/mark-as-read")
    public ResponseEntity<CommonResponse<ChangeReadStatusResult>> markAsRead(Authentication authentication, @PathVariable long notificationId) {
        String email = authentication.getName();
        ChangeReadStatusResult result = notificationService.markAsRead(email, List.of(notificationId));
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "알림 읽지 않음 처리", description = "해당 사용자의 여러 알림을 읽지 않음 상태로 변경합니다. 상태 변경에 성공한 알림 번호를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽지 않음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "모든 알림이 사용자의 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 알림을 찾을 수 없음")
    })
    @PatchMapping("/api/notifications/mark-as-unread")
    public ResponseEntity<CommonResponse<ChangeReadStatusResult>> markAsUnread(Authentication authentication, @RequestBody @Valid ChangeReadStatusRequest request) {
        String email = authentication.getName();
        ChangeReadStatusResult result = notificationService.markAsUnread(email, request.getNotificationIds());
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "알림 단건 읽지 않음 처리", description = "해당 사용자의 특정 알림 하나를 읽지 않음 상태로 변경합니다. 상태 변경에 성공한 알림 번호를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽지 않음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "알림이 사용자의 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 알림을 찾을 수 없음")
    })
    @PatchMapping("/api/notifications/{notificationId}/mark-as-unread")
    public ResponseEntity<CommonResponse<ChangeReadStatusResult>> markAsUnread(Authentication authentication, @PathVariable long notificationId) {
        String email = authentication.getName();
        ChangeReadStatusResult result = notificationService.markAsUnread(email, List.of(notificationId));
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
