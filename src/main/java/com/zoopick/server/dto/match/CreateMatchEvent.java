package com.zoopick.server.dto.match;

import com.zoopick.server.entity.Item;

public record CreateMatchEvent(Long matchId, Item lostItem, Item foundItem) {}