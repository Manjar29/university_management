package com.university.management.repository;

import com.university.management.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCourseCode(String courseCode);
    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);
    Page<Course> findByDepartmentContainingIgnoreCase(String department, Pageable pageable);
}
