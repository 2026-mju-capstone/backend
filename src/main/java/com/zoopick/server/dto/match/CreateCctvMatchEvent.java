package com.zoopick.server.dto.match;

import com.zoopick.server.entity.CctvDetection;
import com.zoopick.server.entity.CctvDetectionMatch;
import com.zoopick.server.entity.Item;

public record CreateCctvMatchEvent(Item item, CctvDetectionMatch cctvDetectionMatch, CctvDetection cctvDetection,
                                   com.zoopick.server.entity.ItemPost itemPost) {
}
