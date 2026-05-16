package com.zoopick.server.dto.cctv;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchedLostItems {
    @JsonProperty("lost_item_id")
    long lostItemId;

    String title;

    ItemCategory category;

    @JsonProperty("match_count")
    int matchCount;

    @JsonProperty("reported_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime reportedAt;

    @JsonProperty("image_url")
    String imageUrl;
}
