package com.zoopick.server.dto.timetable;

public record TimetableGroupResponse(
    Long timetableId,
    String name,
    Boolean isPrimary
) {}
