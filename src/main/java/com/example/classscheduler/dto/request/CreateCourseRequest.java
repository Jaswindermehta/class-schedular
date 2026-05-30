package com.example.classscheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    @Schema(description = "Academic catalog course title", example = "Intro to Java")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Brief course details and curriculum", example = "Learn core OOP structures, loops, and conditions.")
    private String description;
}
