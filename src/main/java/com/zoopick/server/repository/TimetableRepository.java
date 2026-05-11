package com.zoopick.server.repository;

import com.zoopick.server.entity.Timetable;
import com.zoopick.server.entity.TimetableGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    @EntityGraph(attributePaths = {"course", "course.room", "course.room.building"})
    List<Timetable> findAllByTimetableGroup(TimetableGroup timetableGroup);

    @Modifying
    @Query("DELETE FROM Timetable t WHERE t.timetableGroup = :group")
    void deleteAllByTimetableGroup(@Param("group") TimetableGroup group);
}
