package com.zoopick.server.service.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zoopick.server.entity.NotificationType;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public interface NotificationPayload {
    @JsonIgnore
    NotificationType type();

    @JsonIgnore
    Map<String, String> toMap();
}
