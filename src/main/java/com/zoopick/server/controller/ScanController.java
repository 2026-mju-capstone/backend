package com.zoopick.server.controller;

import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.dto.scan.ScanOwnerResult;
import com.zoopick.server.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scan")
@NullMarked
@Tag(name = "Scan API", description = "QR 스캔 기반 사용자 조회 API")
public class ScanController {
    private final ProfileService profileService;

    public ScanController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(
            summary = "QR 소유자 조회",
            description = "QR 코드에 포함된 사용자 식별값으로 소유자의 기본 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ScanOwnerResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/owner/{userId}")
    public ResponseEntity<CommonResponse<ScanOwnerResult>> scanOwner(
            @Parameter(description = "QR 코드에 담긴 사용자 ID", example = "1")
            @PathVariable long userId
    ) {
        String nickname = profileService.findNickname(userId);
        return ResponseEntity.ok(CommonResponse.success(new ScanOwnerResult(userId, nickname)));
    }
}
