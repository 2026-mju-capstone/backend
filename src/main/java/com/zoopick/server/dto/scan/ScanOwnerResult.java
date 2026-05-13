package com.zoopick.server.dto.scan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScanOwnerResult {
    @JsonProperty("owner_id")
    private Long ownerId;
    private String nickname;
}
