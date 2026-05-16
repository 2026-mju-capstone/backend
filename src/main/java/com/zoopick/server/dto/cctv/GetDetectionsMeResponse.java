package com.zoopick.server.dto.cctv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetDetectionsMeResponse {

    @JsonProperty("matched_lost_items")
    private List<MatchedLostItems> matchedLostItems;
}
