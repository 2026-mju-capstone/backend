package com.zoopick.server.service.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.NotificationType;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@AllArgsConstructor
@NullMarked
public class TheftSuspectedPayload implements NotificationPayload {
    @JsonProperty("item_id")
    private final long itemId;

    public static TheftSuspectedPayload of(Item item) {
        return new TheftSuspectedPayload(item.getId());
    }

    @Override
    public NotificationType type() {
        return NotificationType.THEFT_SUSPECTED;
    }

    @Override
    public Map<String, String> toMap() {
        return Map.of(
                "item_id", String.valueOf(itemId)
        );
    }
}
