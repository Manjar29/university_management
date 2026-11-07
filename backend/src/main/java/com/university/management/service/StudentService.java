package com.university.management.service;

import com.university.management.entity.Role;
import com.university.management.entity.Student;
import com.university.management.entity.User;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ForbiddenException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.UserRepository;
import com.university.management.repository.EnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EnrollmentRepository enrollmentRepository;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository, UserService userService,
            EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.enrollmentRepository = enrollmentRepository;
    }

    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Student getById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
    }

    public Student getCurrentStudent() {
        User current = userService.getCurrentUser();
        return studentRepository.findByUserId(current.getId())
                .orElseGet(() -> {
                    Student profile = new Student();
                    profile.setUser(current);
                    profile.setStudentNumber("STU-" + current.getId());
                    profile.setDepartment("Undeclared");
                    profile.setProgram("Undeclared");
                    profile.setSemester(1);
                    profile.setEnrollmentYear(Year.now().getValue());
                    return studentRepository.save(profile);
                });
    }

    public Student createStudent(Student student) {
        if (studentRepository.existsByStudentNumber(student.getStudentNumber())) {
            throw new ConflictException("Student number already exists.");
        }
        if (student.getUser() == null || student.getUser().getId() == null) {
            throw new IllegalArgumentException("Student must be linked to a user account.");
        }
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long id, Student updated) {
        Student existing = getById(id);
        if (updated.getDepartment() != null) existing.setDepartment(updated.getDepartment());
        if (updated.getProgram() != null) existing.setProgram(updated.getProgram());
        if (updated.getSemester() != null) existing.setSemester(updated.getSemester());
        if (updated.getEnrollmentYear() != null) existing.setEnrollmentYear(updated.getEnrollmentYear());
        return studentRepository.save(existing);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = getById(id);
        enrollmentRepository.deleteByStudent(student);
        studentRepository.delete(student);
        studentRepository.flush();
        userRepository.delete(student.getUser());
    }

    public void assertStudentOwnsRecord(Long studentId) {
        User current = userService.getCurrentUser();
        if (current.getRole() == Role.STUDENT && !current.getId().equals(getById(studentId).getUser().getId())) {
            throw new ForbiddenException("Students may only access their own profile.");
        }
    }
}
