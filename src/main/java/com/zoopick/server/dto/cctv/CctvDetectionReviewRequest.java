package com.zoopick.server.dto.cctv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.DetectionReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CctvDetectionReviewRequest {
    @NotNull
    @JsonProperty("review_status")
    DetectionReviewStatus reviewStatus;
}
