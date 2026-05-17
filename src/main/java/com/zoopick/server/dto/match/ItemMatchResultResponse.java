package com.zoopick.server.dto.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.MatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemMatchResultResponse {
    @NotNull
    @JsonProperty("match_id")
    private long matchId;
    @NotNull
    @JsonProperty("found_item_id")
    private long foundItemId;
    @NotNull
    @JsonProperty("found_post_id")
    private long foundPostId;
    @JsonProperty("found_post_title")
    private String foundPostTitle;
    @JsonProperty("found_image_url")
    private String foundImageUrl;
    @JsonProperty("location_name")
    private String locationName;
    @JsonProperty("found_nickname")
    private String foundNickname;
    @JsonProperty("found_department")
    private String foundDepartment;
    private float score;
    private MatchStatus status;
    @JsonProperty("counterpart_id")
    private Long counterpartId;
}
