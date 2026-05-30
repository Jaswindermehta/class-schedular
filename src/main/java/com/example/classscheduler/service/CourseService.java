package com.example.classscheduler.service;

import com.example.classscheduler.dto.request.CreateCourseRequest;
import com.example.classscheduler.dto.response.CourseResponse;
import com.example.classscheduler.entity.Course;
import com.example.classscheduler.exception.ConflictException;
import com.example.classscheduler.mapper.CourseMapper;
import com.example.classscheduler.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        log.info("Creating course with title: {}", request.getTitle());
        // Validate unique course title to avoid confusion
        courseRepository.findAll().stream()
                .filter(c -> c.getTitle().equalsIgnoreCase(request.getTitle()))
                .findAny()
                .ifPresent(c -> {
                    throw new ConflictException("Course with this title already exists");
                });

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponse(savedCourse);
    }
}
