package com.example.classscheduler.mapper;

import com.example.classscheduler.dto.response.CourseResponse;
import com.example.classscheduler.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toResponse(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseResponse(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getCreatedAt()
        );
    }
}
