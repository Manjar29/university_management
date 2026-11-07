package com.university.management.service;

import com.university.management.entity.Course;
import com.university.management.entity.Role;
import com.university.management.entity.Teacher;
import com.university.management.entity.User;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ForbiddenException;
import com.university.management.repository.CourseRepository;
import com.university.management.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserService userService;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, teacherRepository, userService);
    }

    @Test
    void adminCanCreateCourseForSelectedTeacher() {
        User admin = new User();
        admin.setRole(Role.ADMIN);
        Teacher teacher = new Teacher();
        teacher.setId(3L);
        Course course = new Course();
        course.setCourseCode("CS101");
        course.setTeacher(teacher);

        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(admin);
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(courseRepository.save(course)).thenReturn(course);

        Course created = courseService.createCourse(course);

        assertSame(teacher, created.getTeacher());
        verify(courseRepository).save(course);
    }

    @Test
    void createCourseRejectsDuplicateCourseCode() {
        Course course = new Course();
        course.setCourseCode("CS101");
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        assertThrows(ConflictException.class, () -> courseService.createCourse(course));
        verify(courseRepository, never()).save(any(Course.class));
        verifyNoInteractions(userService, teacherRepository);
    }

    @Test
    void teacherCannotUpdateAnotherTeachersCourse() {
        User teacherUser = new User();
        teacherUser.setId(11L);
        teacherUser.setRole(Role.TEACHER);
        User otherUser = new User();
        otherUser.setId(22L);

        Teacher courseTeacher = new Teacher();
        courseTeacher.setUser(otherUser);
        Course course = new Course();
        course.setTeacher(courseTeacher);

        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(teacherUser);

        assertThrows(ForbiddenException.class, () -> courseService.updateCourse(5L, new Course()));
        verify(courseRepository, never()).save(any(Course.class));
    }
}
