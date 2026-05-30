package com.example.classscheduler.mapper;

import com.example.classscheduler.dto.response.TeacherResponse;
import com.example.classscheduler.entity.Teacher;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper {

    public TeacherResponse toResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherResponse(
            teacher.getId(),
            teacher.getName(),
            teacher.getEmail(),
            teacher.getTimezone(),
            teacher.getCreatedAt()
        );
    }
}
