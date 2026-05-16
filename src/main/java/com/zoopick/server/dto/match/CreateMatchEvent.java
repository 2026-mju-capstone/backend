package com.zoopick.server.dto.match;

import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.ItemMatch;

public record CreateMatchEvent(ItemMatch match, Item lostItem, Item foundItem) {}