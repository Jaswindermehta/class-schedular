package com.example.classscheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeacherRequest {

    @Schema(description = "Full name of the instructor", example = "Sarah Miller")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Email address of the instructor", example = "sarah@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    @Schema(description = "Instructor's local timezone ID", example = "America/New_York")
    @NotBlank(message = "Timezone is required")
    private String timezone;
}
