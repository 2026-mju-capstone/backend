package com.zoopick.server.repository;

import com.zoopick.server.entity.Timetable;
import com.zoopick.server.entity.TimetableGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findAllByTimetableGroup(TimetableGroup timetableGroup);
    void deleteAllByTimetableGroup(TimetableGroup timetableGroup);
}
