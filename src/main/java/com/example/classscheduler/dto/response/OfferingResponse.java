package com.example.classscheduler.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingResponse {
    private Long id;
    private String name;
    private String timezone;
    private Long courseId;
    private String courseTitle;
    private Long teacherId;
    private String teacherName;
    private OffsetDateTime createdAt;
}
