package com.example.classscheduler.mapper;

import com.example.classscheduler.dto.response.OfferingResponse;
import com.example.classscheduler.entity.Offering;
import org.springframework.stereotype.Component;

@Component
public class OfferingMapper {

    public OfferingResponse toResponse(Offering offering) {
        if (offering == null) {
            return null;
        }

        Long courseId = offering.getCourse() != null ? offering.getCourse().getId() : null;
        String courseTitle = offering.getCourse() != null ? offering.getCourse().getTitle() : null;
        
        Long teacherId = offering.getTeacher() != null ? offering.getTeacher().getId() : null;
        String teacherName = offering.getTeacher() != null ? offering.getTeacher().getName() : null;

        return new OfferingResponse(
            offering.getId(),
            offering.getName(),
            offering.getTimezone(),
            courseId,
            courseTitle,
            teacherId,
            teacherName,
            offering.getCreatedAt()
        );
    }
}
