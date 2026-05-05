package com.zoopick.server.controller;

import com.zoopick.server.dto.profile.ProfileSummaryResponse;
import com.zoopick.server.dto.profile.ProfileUpdateRequest;
import com.zoopick.server.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile API", description = "사용자 프로필 관련 API")
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "내 프로필 요약 조회", description = "현재 로그인한 사용자의 프로필 기본 정보와 활동 요약(게시글 수, 채팅방 수, 안 읽은 알림 수)을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileSummaryResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        ProfileSummaryResponse response = profileService.getProfileSummary(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 정보 수정", description = "현재 로그인한 사용자의 닉네임과 학과 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 이미 존재하는 닉네임인 경우"), // @Valid 예외 고려
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없는 경우")
    })
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            Authentication authentication,
            @RequestBody @Valid ProfileUpdateRequest request) { // @Valid 적용

        profileService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}