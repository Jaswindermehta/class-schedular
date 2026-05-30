package com.example.classscheduler.controller;

import com.example.classscheduler.dto.request.CreateParentRequest;
import com.example.classscheduler.dto.response.ParentResponse;
import com.example.classscheduler.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Parents & Profiles", description = "Endpoints for managing parent registration profiles")
@RestController
@RequestMapping("/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @Operation(summary = "Onboard a new parent profile")
    @PostMapping
    public ResponseEntity<ParentResponse> createParent(@Valid @RequestBody CreateParentRequest request) {
        ParentResponse response = parentService.createParent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
