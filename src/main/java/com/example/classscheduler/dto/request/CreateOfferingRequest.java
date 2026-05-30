package com.example.classscheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferingRequest {

    @Schema(description = "Database course entity reference ID", example = "1")
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @Schema(description = "Database teacher entity reference ID", example = "1")
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @Schema(description = "Unique moniker for this class cohort offering", example = "Intro Java - Summer Mon Cohort")
    @NotBlank(message = "Offering name is required")
    private String name;

    @Schema(description = "Cohort default schedule timezone context", example = "America/New_York")
    @NotBlank(message = "Timezone is required")
    private String timezone;
}
