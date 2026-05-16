package com.zoopick.server.controller;

import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.dto.cctv.*;
import com.zoopick.server.dto.cctv.GetDetectionByItemIdResponse;
import com.zoopick.server.security.UserPrincipal;
import com.zoopick.server.service.CctvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "CCTV API", description = "CCTV 영상 등록 및 분석 큐 관리")
@RestController
@RequestMapping("/api/cctv")
@RequiredArgsConstructor
public class CctvController {
    private final CctvService cctvService;

    @Operation(
            summary = "CCTV 영상 등록 + 분석 큐 등록",
            description = """
            CCTV 영상 메타데이터를 등록하고 즉시 분석 큐에 추가합니다.
            
            등록 후 백그라운드 워커가 큐 순서대로 영상을 분석하며,
            검출 결과는 cctv_detections 테이블에 캐싱됩니다.
            
            응답에는 큐에서의 위치와 예상 시작 시각이 포함됩니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "등록 + 큐 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (room_id 없음, video_url 형식 오류 등)"),
    })
    @PostMapping("/videos")
    public ResponseEntity<CommonResponse<CctvVideoCreateResponse>> createVideoAndEnqueue(@Valid @RequestBody CctvVideoCreateRequest request) {
        CctvVideoCreateResponse response = cctvService.createVideoAndEnqueue(request);
        return ResponseEntity.accepted().body(CommonResponse.success(response));
    }

    @Operation(
            summary = "기존 영상 재큐잉",
            description = """
            이미 등록된 CCTV 영상을 다시 분석 큐에 등록합니다.
            
            분석 실패(FAILED) 후 재시도하거나, 우선순위 변경이 필요할 때 사용합니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "재큐잉 성공"),
            @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음"),
    })
    @PostMapping("/enqueue/{videoId}")
    public ResponseEntity<CommonResponse<CctvEnqueueResponse>> enqueueVideo(@PathVariable Long videoId) {
        CctvEnqueueResponse response = cctvService.enqueueVideo(videoId);
        return ResponseEntity.accepted().body(CommonResponse.success(response));
    }

    @Operation(
            summary = "CCTV 분석 결과 전체 조회",
            description = """
            관리자가 CCTV 분석 결과를 조회합니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/videos")
    public ResponseEntity<CommonResponse<List<GetCctvVideoResponse>>> getVideos() {
        List<GetCctvVideoResponse> response = cctvService.getCctvVideos();
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
            summary = "CCTV 분석 물품 전체 조회",
            description = """
            관리자가 CCTV 분석 물품 리스트를 조회합니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/detections")
    public ResponseEntity<CommonResponse<List<GetAllDetectionResponse>>> getAllDetection() {
        List<GetAllDetectionResponse> response = cctvService.getAllCctvDetection();
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
            summary = "CCTV 분석 물품 상세 조회",
            description = """
            관리자가 CCTV 분석 물품을 상세 조회합니다.
            embedding은 조회하지 않습니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "물품을 찾을 수 없음")
    })
    @GetMapping("/detections/{id}")
    public ResponseEntity<CommonResponse<GetDetectionByIdResponse>> getDetection(@PathVariable Long id) {
        GetDetectionByIdResponse response = cctvService.getCctvDetectionById(id);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
            summary = "나의 CCTV 매칭 현황 및 상세 조회",
            description = """
        유저가 등록한 분실물들과 CCTV AI 분석으로 매칭된 결과들을 조회합니다.
        
        1. 일반 호출: CCTV 매칭 이력이 있는 나의 '분실물 목록'을 조회합니다.
        2. itemId 호출: 특정 분실물에 대해 매칭된 'CCTV 상세 탐지 정보(시간, 장소, 스코어 등)' 목록을 조회합니다.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 만료 또는 없음)"),
            @ApiResponse(responseCode = "404", description = "유저 또는 해당 아이템 정보를 찾을 수 없음")
    })
    @GetMapping("/detections/me")
    public ResponseEntity<CommonResponse<?>> getDetectionsMe(
            @Parameter(description = "조회할 유저")
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "itemId", required = false) Long itemId) {
        if (itemId == null) {
            GetDetectionsMeResponse response = cctvService.getDetectionsMe(principal.id());
            return ResponseEntity.ok(CommonResponse.success(response));
        }

        GetDetectionByItemIdResponse response = cctvService.getDetectionsMeByItemId(principal.id(), itemId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
            summary = "CCTV 매칭 결과 검토(확정/거절)",
            description = """
        사용자가 본인의 분실물과 매칭된 CCTV 탐지 결과를 검토하고 상태를 업데이트합니다.
        
        상태 값:
        - CONFIRMED_SELF: 내 물건이 맞음 (도난 의심)
        - REJECTED_SELF: 내 물건이 아님 (도난 아님)
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검토 결과 반영 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "해당 탐지 결과에 대한 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "탐지 정보를 찾을 수 없음")
    })
    @PutMapping("/detections/{matchId}/review")
    public ResponseEntity<CommonResponse<?>> reviewMatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "리뷰할 매칭 ID") @PathVariable Long matchId,
            @Valid @RequestBody CctvDetectionReviewRequest request
    ) {
        cctvService.reviewMatch(principal.id(), matchId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(
            summary = "CCTV 업로드",
            description = """
            관리자가 CCTV를 업로드합니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/upload")
    public ResponseEntity<CommonResponse<String>> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        String path = cctvService.uploadVideo(file);
        return ResponseEntity.ok(CommonResponse.success(path));
    }
}
