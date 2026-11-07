package com.university.management.controller;

import com.university.management.entity.Enrollment;
import com.university.management.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Enrollment>> getAllEnrollments(Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments(pageable));
    }

    @GetMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Enrollment> getEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getById(id));
    }

    @PostMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Enrollment> createEnrollment(@RequestParam Long studentId, @RequestParam Long courseId) {
        return ResponseEntity.ok(enrollmentService.createEnrollment(studentId, courseId));
    }

    @PatchMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Enrollment> updateEnrollmentGrade(@PathVariable Long id, @RequestParam BigDecimal grade) {
        return ResponseEntity.ok(enrollmentService.updateGrade(id, grade));
    }

    @DeleteMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
