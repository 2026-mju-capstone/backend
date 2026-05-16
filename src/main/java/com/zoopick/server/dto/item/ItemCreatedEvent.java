package com.zoopick.server.dto.item;

import com.zoopick.server.entity.ItemType;

public record ItemCreatedEvent(Long itemId, ItemType itemType) {
}
