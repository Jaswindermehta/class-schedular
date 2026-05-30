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
public class TeacherResponse {
    private Long id;
    private String name;
    private String email;
    private String timezone;
    private OffsetDateTime createdAt;
}
