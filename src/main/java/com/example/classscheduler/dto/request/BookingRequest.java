package com.example.classscheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @Schema(description = "Database parent entity reference ID", example = "1")
    @NotNull(message = "Parent ID is required")
    private Long parentId;

    @Schema(description = "Database offering entity reference ID", example = "1")
    @NotNull(message = "Offering ID is required")
    private Long offeringId;
}
