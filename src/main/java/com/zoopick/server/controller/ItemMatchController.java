package com.zoopick.server.controller;

import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.dto.item.ItemMatchResultResponse;
import com.zoopick.server.security.UserPrincipal;
import com.zoopick.server.service.ItemMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Item Matching API", description = "아이템 매칭용 API")
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor

public class ItemMatchController {
    private final ItemMatchService itemMatchService;

    @Operation(summary = "매칭 조회", description = "유저의 잃어버린 아이템과 매칭된 아이템을 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 조회 성공"),
            @ApiResponse(responseCode = "404", description = "매칭을 찾을 수 없음")
    })
    @GetMapping("/me")
    public CommonResponse<List<ItemMatchResultResponse>> itemMatchingResult(
            @Parameter(description = "조회할 유저")
            @AuthenticationPrincipal UserPrincipal principal) {
        return CommonResponse.success(itemMatchService.getItemMatchResult(principal.id()));
    }
}
