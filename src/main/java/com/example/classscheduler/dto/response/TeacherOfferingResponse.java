package com.example.classscheduler.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherOfferingResponse {
    private Long id;
    private String name;
    private String timezone;
    private Long courseId;
    private String courseTitle;
    private OffsetDateTime createdAt;
    private List<SessionResponse> sessions;
}
