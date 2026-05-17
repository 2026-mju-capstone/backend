package com.zoopick.server.controller;

import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.dto.metadata.BuildingRecord;
import com.zoopick.server.dto.metadata.RoomRecord;
import com.zoopick.server.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Metadata API", description = "메타데이터 조회 API")
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
@NullMarked
public class MetadataController {
    private final MetadataService metadataService;

    @Operation(summary = "건물 목록 조회", description = "게시글 작성 및 조회에 사용하는 건물 이름 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "건물 목록 조회 성공")
    })
    @GetMapping("/buildings")
    public ResponseEntity<CommonResponse<List<BuildingRecord>>> getBuildings() {
        return ResponseEntity.ok(CommonResponse.success(metadataService.getBuildings()));
    }

    @Operation(summary = "강의실 목록 조회", description = "특정 건물의 강의실 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의실 목록 조회 성공")
    })
    @GetMapping("/buildings/{buildingId}/rooms")
    public ResponseEntity<CommonResponse<List<RoomRecord>>> getRooms(@PathVariable Long buildingId) {
        return ResponseEntity.ok(CommonResponse.success(metadataService.getRooms(buildingId)));
    }

    @Operation(summary = "물품 카테고리 목록 조회", description = "게시글 작성 및 조회에 사용하는 물품 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "물품 카테고리 목록 조회 성공")
    })
    @GetMapping("/item-categories")
    public ResponseEntity<CommonResponse<List<String>>> getItemCategories() {
        List<String> itemCategories = metadataService.getItemCategories();
        return ResponseEntity.ok(CommonResponse.success(itemCategories));
    }
}
