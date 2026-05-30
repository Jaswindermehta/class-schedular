package com.example.classscheduler.mapper;

import com.example.classscheduler.dto.response.ParentResponse;
import com.example.classscheduler.entity.Parent;
import org.springframework.stereotype.Component;

@Component
public class ParentMapper {

    public ParentResponse toResponse(Parent parent) {
        if (parent == null) {
            return null;
        }
        return new ParentResponse(
            parent.getId(),
            parent.getName(),
            parent.getEmail(),
            parent.getTimezone(),
            parent.getCreatedAt()
        );
    }
}
