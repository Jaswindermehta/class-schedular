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
public class BookingResponse {
    private Long id;
    private Long parentId;
    private String parentName;
    private Long offeringId;
    private String offeringName;
    private OffsetDateTime bookedAt;
    private List<SessionResponse> sessions;
}
