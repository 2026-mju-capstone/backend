package com.zoopick.server.service;

import com.zoopick.server.dto.match.CctvMatchCriteria;
import com.zoopick.server.entity.*;
import com.zoopick.server.repository.CourseScheduleRepository;
import com.zoopick.server.repository.RoomRepository;
import com.zoopick.server.repository.TimetableGroupRepository;
import com.zoopick.server.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CctvMatchCriteriaResolver {
    private final TimetableGroupRepository timetableGroupRepository;
    private final TimetableRepository timetableRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final RoomRepository roomRepository;

    public CctvMatchCriteria resolve(Item lostItem) {
        if (lostItem.getReportedBuilding() == null) {
            return new CctvMatchCriteria(List.of(),
                    lostItem.getReportedAt() != null ? lostItem.getReportedAt() : LocalDateTime.now());
        }

        LocalDateTime reportedTime = lostItem.getReportedAt() != null ? lostItem.getReportedAt() : LocalDateTime.now();
        DayOfWeek targetDay = DayOfWeek.valueOf(reportedTime.getDayOfWeek().name().substring(0, 3));
        LocalTime targetLocalTime = reportedTime.toLocalTime();

        boolean hasTimetableMatch = false;
        LocalDateTime searchStartTime = reportedTime;
        var primaryGroup = timetableGroupRepository.findByUserAndIsPrimaryTrue(lostItem.getReporter());

        if (primaryGroup.isPresent()) {
            List<Timetable> timetables = timetableRepository.findAllByTimetableGroup(primaryGroup.get());
            List<Course> buildingCourses = timetables.stream()
                    .map(Timetable::getCourse)
                    .filter(c -> c.getRoom().getBuilding().getId().equals(lostItem.getReportedBuilding().getId()))
                    .toList();

            if (!buildingCourses.isEmpty()) {
                List<CourseSchedule> schedules = courseScheduleRepository.findAllByCourseIn(buildingCourses);
                var matchedSchedule = schedules.stream()
                        .filter(s -> s.getDayOfWeek() == targetDay &&
                                !targetLocalTime.isBefore(s.getStartTime()) &&
                                !targetLocalTime.isAfter(s.getEndTime()))
                        .findFirst();

                if (matchedSchedule.isPresent()) {
                    hasTimetableMatch = true;
                    searchStartTime = LocalDateTime.of(reportedTime.toLocalDate(), matchedSchedule.get().getStartTime());
                }
            }
        }

        var reportedRoom = roomRepository.findByNameAndBuilding(lostItem.getLocationName(), lostItem.getReportedBuilding());

        if (reportedRoom.isPresent() && hasTimetableMatch) {
            return new CctvMatchCriteria(List.of(reportedRoom.get().getId()), searchStartTime);
        } else {
            List<Long> allRoomIds = roomRepository.findAllByBuilding(lostItem.getReportedBuilding())
                    .stream()
                    .map(Room::getId)
                    .toList();
            return new CctvMatchCriteria(allRoomIds, reportedTime);
        }
    }
}
