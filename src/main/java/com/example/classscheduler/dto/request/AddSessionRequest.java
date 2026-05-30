package com.example.classscheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddSessionRequest {

    @Schema(description = "Session start calendar date/time in teacher's local timezone context", example = "2026-06-01T09:00:00")
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @Schema(description = "Session end calendar date/time in teacher's local timezone context", example = "2026-06-01T10:30:00")
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
