package com.zoopick.server.dto.timetable;

import com.zoopick.server.entity.Course;
import java.time.LocalTime;

public record TimetableCourseResponse(
    Long courseId,
    String courseName,
    Course.DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String roomName,
    String buildingName,
    String buildingCode,
    String color
) {}
