package com.zoopick.server.service.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.ItemPost;
import com.zoopick.server.entity.NotificationType;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@AllArgsConstructor
@NullMarked
public class ItemReturnedPayload implements NotificationPayload {
    @JsonProperty("item_id")
    private final long itemId;
    @JsonProperty("item_post_id")
    private final long itemPostId;

    public static ItemReturnedPayload of(ItemPost itemPost) {
        return new ItemReturnedPayload(itemPost.getItem().getId(), itemPost.getId());
    }

    @Override
    public NotificationType type() {
        return NotificationType.ITEM_RETURNED;
    }

    @Override
    public Map<String, String> toMap() {
        return Map.of(
                "item_id", String.valueOf(itemId),
                "item_post_id", String.valueOf(itemPostId)
        );
    }
}
