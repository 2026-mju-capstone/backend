package com.zoopick.server.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChangeReadStatusResult {
    @JsonProperty("succeed_ids")
    private List<Long> succeedIds;
}
