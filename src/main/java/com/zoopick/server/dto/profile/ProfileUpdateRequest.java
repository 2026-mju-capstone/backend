package com.zoopick.server.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(

        @NotBlank
        String nickname,

        @NotBlank
        String department
) {
}
