package com.zoopick.server.dto.timetable;

import java.util.List;

public record TimetableSyncRequest(
    List<CourseColorRequest> courses
) {
    public record CourseColorRequest(
        Long courseId,
        String color
    ) {}
}
