package com.zoopick.server.dto.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.ItemCategory;
import com.zoopick.server.entity.ItemColor;
import com.zoopick.server.entity.ItemStatus;
import com.zoopick.server.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ItemPostRecord {
    private long id;
    private String title;
    private String description;
    private ItemType type;
    private ItemStatus status;
    private ItemCategory category;
    private ItemColor color;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("building_id")
    private long buildingId;
    @JsonProperty("data_address")
    private String detailAddress;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
