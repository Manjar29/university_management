package com.university.management.service;

import com.university.management.entity.Course;
import com.university.management.entity.Enrollment;
import com.university.management.entity.Student;
import com.university.management.entity.Role;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.CourseRepository;
import com.university.management.repository.EnrollmentRepository;
import com.university.management.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
            CourseRepository courseRepository, UserService userService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    public Page<Enrollment> getAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll(pageable);
    }

    public Enrollment getById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found."));
    }

    @Transactional
    public Enrollment createEnrollment(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        if (userService.getCurrentUser().getRole() == Role.STUDENT
                && !student.getUser().getId().equals(userService.getCurrentUser().getId())) {
            throw new com.university.management.exception.ForbiddenException("Students may only manage their own enrollments.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new ConflictException("Student is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(java.time.LocalDate.now());
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment updateGrade(Long id, BigDecimal grade) {
        Enrollment enrollment = getById(id);
        enrollment.setGrade(grade);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        enrollmentRepository.deleteById(id);
    }
}
