package com.university.management.repository;

import com.university.management.entity.Course;
import com.university.management.entity.Enrollment;
import com.university.management.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
    List<Enrollment> findByCourseId(Long courseId);
    boolean existsByStudentAndCourse(Student student, Course course);

    @Modifying
    @Transactional
    void deleteByStudent(Student student);
}
