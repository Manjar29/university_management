package com.university.management.controller;

import com.university.management.entity.Student;
import com.university.management.entity.User;
import com.university.management.service.StudentService;
import com.university.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    private final UserService userService;

    public StudentController(StudentService studentService, UserService userService) {
        this.studentService = studentService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Student>> getAllStudents(Pageable pageable) {
        return ResponseEntity.ok(studentService.getAllStudents(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and @studentService.getById(#id).user.id == principal.id)")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Student> getMe() {
        return ResponseEntity.ok(studentService.getCurrentStudent());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Student> updateMe(@RequestBody Student updated) {
        User current = userService.getCurrentUser();
        Student existing = studentService.getCurrentStudent();
        if (!existing.getUser().getId().equals(current.getId())) {
            throw new IllegalStateException("Forbidden student access.");
        }
        return ResponseEntity.ok(studentService.updateStudent(existing.getId(), updated));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        return ResponseEntity.status(201).body(studentService.createStudent(student));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and @studentService.getById(#id).user.id == principal.id)")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        return ResponseEntity.ok(studentService.updateStudent(id, student));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
