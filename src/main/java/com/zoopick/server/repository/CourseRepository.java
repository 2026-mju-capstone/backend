package com.zoopick.server.repository;

import com.zoopick.server.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = {"room", "room.building"})
    @Query("SELECT c FROM Course c WHERE c.year = :year AND c.semester = :semester " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword%)")
    Page<Course> searchCourses(@Param("year") Integer year, 
                               @Param("semester") Integer semester, 
                               @Param("keyword") String keyword, 
                               Pageable pageable);
}
