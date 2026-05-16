package com.zoopick.server.dto.cctv;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CctvDetectionDetail {
    @JsonProperty("match_id")
    private Long matchId;

    private double score;

    @JsonProperty("detected_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime detectedAt;

    @JsonProperty("building_name")
    private String buildingName;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("item_snapshot_url")
    private String itemSnapshotUrl;

    @JsonProperty("moment_snapshot_url")
    private String momentSnapshotUrl;
}
