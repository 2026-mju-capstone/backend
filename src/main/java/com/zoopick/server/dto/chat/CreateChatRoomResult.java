package com.zoopick.server.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRoomResult {
    private boolean created;

    @NotBlank
    @JsonProperty("room_data")
    private ChatRoomRecord roomData;
}
