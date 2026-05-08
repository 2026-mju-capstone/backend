package com.zoopick.server.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomRecord {
    @JsonProperty("room_id")
    private long roomId;
    @JsonProperty("owner_nickname")
    private String ownerNickname;
    @JsonProperty("finder_nickname")
    private String finderNickname;
    @JsonProperty("item_name")
    private String itemName;
}
