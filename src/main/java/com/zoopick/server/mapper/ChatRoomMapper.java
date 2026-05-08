package com.zoopick.server.mapper;

import com.zoopick.server.dto.chat.ChatRoomRecord;
import com.zoopick.server.entity.ChatRoom;
import com.zoopick.server.entity.Item;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class ChatRoomMapper {
    public ChatRoomRecord toChatRoomRecord(ChatRoom chatRoom) {
        return ChatRoomRecord.builder()
                .roomId(chatRoom.getId())
                .ownerNickname(chatRoom.getOwner().getNickname())
                .finderNickname(chatRoom.getFinder().getNickname())
                .itemName(resolveItemDetail(chatRoom.getItem()))
                .build();
    }

    private String resolveItemDetail(@Nullable Item item) {
        if (item == null)
            return "";
        return item.getCategory().getDisplayName();
    }
}
