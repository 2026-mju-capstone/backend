package com.zoopick.server.service.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.ItemMatch;
import com.zoopick.server.entity.NotificationType;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@AllArgsConstructor
@NullMarked
public class MatchFoundPayload implements NotificationPayload {
    @JsonProperty("item_id")
    private final long itemId;
    @JsonProperty("match_id")
    private final long matchId;
    private final float score;

    public static MatchFoundPayload of(Item item, ItemMatch itemMatch) {
        return new MatchFoundPayload(item.getId(), itemMatch.getId(), itemMatch.getScore());
    }

    @Override
    public NotificationType type() {
        return NotificationType.MATCH_FOUND;
    }

    @Override
    public Map<String, String> toMap() {
        return Map.of(
                "item_id", String.valueOf(itemId),
                "match_id", String.valueOf(matchId),
                "score", String.valueOf(score)
        );
    }
}
