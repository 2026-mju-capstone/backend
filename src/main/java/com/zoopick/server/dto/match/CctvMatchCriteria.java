package com.zoopick.server.dto.match;

import java.time.LocalDateTime;
import java.util.List;

public record CctvMatchCriteria(
        List<Long> roomIds,
        LocalDateTime searchStartTime
) {}
