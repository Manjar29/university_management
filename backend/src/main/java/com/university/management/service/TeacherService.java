package com.university.management.service;

import com.university.management.entity.Teacher;
import com.university.management.entity.User;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserService userService;

    public TeacherService(TeacherRepository teacherRepository, UserService userService) {
        this.teacherRepository = teacherRepository;
        this.userService = userService;
    }

    public Page<Teacher> getAllTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    public Teacher getById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found."));
    }

    public Teacher getCurrentTeacher() {
        User current = userService.getCurrentUser();
        return teacherRepository.findByUserId(current.getId())
                .orElseGet(() -> {
                    Teacher profile = new Teacher();
                    profile.setUser(current);
                    profile.setEmployeeNumber("TCH-" + current.getId());
                    profile.setDepartment("Undeclared");
                    profile.setDesignation("Teacher");
                    profile.setSpecialization("General");
                    return teacherRepository.save(profile);
                });
    }

    public Teacher createTeacher(Teacher teacher) {
        if (teacherRepository.existsByEmployeeNumber(teacher.getEmployeeNumber())) {
            throw new ConflictException("Employee number already exists.");
        }
        return teacherRepository.save(teacher);
    }

    @Transactional
    public Teacher updateTeacher(Long id, Teacher updated) {
        Teacher existing = getById(id);
        if (updated.getDepartment() != null) existing.setDepartment(updated.getDepartment());
        if (updated.getDesignation() != null) existing.setDesignation(updated.getDesignation());
        if (updated.getSpecialization() != null) existing.setSpecialization(updated.getSpecialization());
        return teacherRepository.save(existing);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = getById(id);
        teacherRepository.delete(teacher);
    }
}
