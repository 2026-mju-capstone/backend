package com.zoopick.server.dto.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.ItemCategory;
import com.zoopick.server.entity.ItemColor;
import com.zoopick.server.entity.ItemType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CreateItemPostRequest {
    @NotNull
    private ItemType type;

    @NotNull
    private ItemCategory category;

    @NotNull
    private ItemColor color;

    private String title;

    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @NotNull
    @JsonProperty("building_id")
    private long buildingId;

    @JsonProperty("detail_address")
    private String detailAddress;

    @JsonProperty("reported_at")
    private LocalDateTime reportedAt;
}
