package com.zoopick.server.dto.cctv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetDetectionByItemIdResponse {
    List<CctvDetectionDetail> detections;
}
