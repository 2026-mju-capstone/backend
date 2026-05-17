package com.zoopick.server.controller;

import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.dto.item.*;
import com.zoopick.server.entity.ItemType;
import com.zoopick.server.security.UserPrincipal;
import com.zoopick.server.service.ItemPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Item Post API", description = "분실물/습득물 게시글 생성 및 조회 API")
@RestController
@RequestMapping("/api/items")
@NullMarked
public class ItemPostController {
    private final ItemPostService itemPostService;

    public ItemPostController(ItemPostService itemPostService) {
        this.itemPostService = itemPostService;
    }

    @Operation(summary = "게시글 생성", description = "분실물 또는 습득물 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청값"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/post/create")
    public ResponseEntity<CommonResponse<CreateItemPostResult>> createItemPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateItemPostRequest request
    ) {
        CreateItemPostResult result = itemPostService.createItemPost(principal.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(result));
    }

    @Operation(summary = "게시글 목록 조회", description = "필터와 페이지 정보를 기준으로 게시글 목록을 조회합니다. 모든 필터 값은 선택입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청값")
    })
    @PostMapping("/post/list")
    public ResponseEntity<CommonResponse<ListItemPostResult>> getItemPosts(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @RequestBody(required = false) @Nullable ItemPostFilter filter
    ) {
        Pageable pageable = PageRequest.of(page, size);
        ListItemPostResult result = itemPostService.getItemPosts(filter, pageable);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "게시글 단건 조회", description = "게시글 ID로 분실물/습득물 게시글 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/post/list/{id}")
    public ResponseEntity<CommonResponse<ItemPostRecord>> getItemPost(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable long id
    ) {
        ItemPostRecord result = itemPostService.getItemPost(id);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "내 게시글 목록 조회", description = "현재 로그인한 사용자가 등록한 분실물(LOST) 또는 습득물(FOUND) 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<List<ItemPostRecord>>> getMyItemPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "조회할 게시글 타입 (LOST 또는 FOUND)", example = "LOST")
            @RequestParam ItemType type
    ) {
        List<ItemPostRecord> result = itemPostService.getMyItemPosts(principal.id(), type);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Operation(summary = "물품 소유자 정보 조회", description = "QR 스캔 시 해당 물품의 소유자(신고자) 정보를 조회합니다. 권한 확인이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "물품을 찾을 수 없음")
    })
    @GetMapping("/{itemId}/owner-info")
    public ResponseEntity<CommonResponse<ItemOwnerInfoResult>> getOwnerInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId
    ) {
        ItemOwnerInfoResult result = itemPostService.getOwnerInfo(principal.id(), itemId);
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
