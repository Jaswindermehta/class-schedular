package com.example.classscheduler.controller;

import com.example.classscheduler.dto.request.AddSessionRequest;
import com.example.classscheduler.dto.request.CreateOfferingRequest;
import com.example.classscheduler.dto.response.OfferingResponse;
import com.example.classscheduler.dto.response.SessionResponse;
import com.example.classscheduler.dto.response.TeacherOfferingResponse;
import com.example.classscheduler.exception.BadRequestException;
import com.example.classscheduler.service.OfferingService;
import com.example.classscheduler.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Tag(name = "Offerings & Schedules", description = "Endpoints for course scheduling and calendar previews")
@RestController
public class OfferingController {

    private final OfferingService offeringService;
    private final SessionService sessionService;

    public OfferingController(OfferingService offeringService, SessionService sessionService) {
        this.offeringService = offeringService;
        this.sessionService = sessionService;
    }

    @Operation(summary = "Create a new course offering cohort")
    @PostMapping("/offerings")
    public ResponseEntity<OfferingResponse> createOffering(@Valid @RequestBody CreateOfferingRequest request) {
        OfferingResponse response = offeringService.createOffering(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Publish scheduled sessions in bulk for a cohort")
    @PostMapping("/offerings/{offeringId}/sessions")
    public ResponseEntity<List<SessionResponse>> addSessions(
            @PathVariable Long offeringId,
            @RequestBody List<AddSessionRequest> requests) {
        List<SessionResponse> response = sessionService.addSessions(offeringId, requests);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get the schedules calendar for a specific teacher's offerings")
    @GetMapping("/teachers/{teacherId}/offerings")
    public ResponseEntity<List<TeacherOfferingResponse>> getTeacherOfferings(@PathVariable Long teacherId) {
        List<TeacherOfferingResponse> response = offeringService.getOfferingsByTeacher(teacherId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get catalog offerings with session calendar projected in local timezone")
    @GetMapping("/offerings")
    public ResponseEntity<List<TeacherOfferingResponse>> getAllOfferings(
            @RequestParam(required = false) String timezone) {
        
        ZoneId targetZone = ZoneOffset.UTC;
        if (timezone != null && !timezone.trim().isEmpty()) {
            try {
                targetZone = ZoneId.of(timezone.trim());
            } catch (Exception ex) {
                throw new BadRequestException("Invalid timezone query parameter: " + timezone);
            }
        }

        List<TeacherOfferingResponse> response = offeringService.getAllOfferings(targetZone);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
