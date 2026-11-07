package com.university.management.service;

import com.university.management.entity.Course;
import com.university.management.entity.Role;
import com.university.management.entity.Teacher;
import com.university.management.entity.User;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.CourseRepository;
import com.university.management.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final UserService userService;

    public CourseService(CourseRepository courseRepository, TeacherRepository teacherRepository, UserService userService) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.userService = userService;
    }

    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Page<Course> getCurrentTeacherCourses(Pageable pageable) {
        Teacher teacher = currentTeacher();
        return courseRepository.findByTeacherId(teacher.getId(), pageable);
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
    }

    @Transactional
    public Course createCourse(Course course) {
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new ConflictException("Course code already exists.");
        }
        Teacher teacher = userService.getCurrentUser().getRole() == Role.TEACHER
                ? currentTeacher()
                : teacherRepository.findById(course.getTeacher().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found."));
        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course updated) {
        Course course = getById(id);
        assertCanManage(course);
        if (updated.getCourseCode() != null) course.setCourseCode(updated.getCourseCode());
        if (updated.getTitle() != null) course.setTitle(updated.getTitle());
        if (updated.getDescription() != null) course.setDescription(updated.getDescription());
        if (updated.getCreditHours() != null) course.setCreditHours(updated.getCreditHours());
        if (updated.getDepartment() != null) course.setDepartment(updated.getDepartment());
        if (updated.getTeacher() != null) {
            Teacher teacher = teacherRepository.findById(updated.getTeacher().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found."));
            course.setTeacher(teacher);
        }
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getById(id);
        assertCanManage(course);
        courseRepository.delete(course);
    }

    private Teacher currentTeacher() {
        User current = userService.getCurrentUser();
        return teacherRepository.findByUserId(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found."));
    }

    private void assertCanManage(Course course) {
        if (userService.getCurrentUser().getRole() == Role.TEACHER
                && !course.getTeacher().getUser().getId().equals(userService.getCurrentUser().getId())) {
            throw new com.university.management.exception.ForbiddenException("Teachers may only manage their own courses.");
        }
    }
}
