package com.zoopick.server.dto.match;

import com.zoopick.server.entity.CctvDetection;
import com.zoopick.server.entity.CctvDetectionMatch;
import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.ItemPost;
import com.zoopick.server.entity.Room;

public record CreateCctvMatchEvent(Item item, CctvDetectionMatch cctvDetectionMatch, CctvDetection cctvDetection, ItemPost itemPost, Room room) {
}
