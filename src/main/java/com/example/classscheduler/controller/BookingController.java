package com.example.classscheduler.controller;

import com.example.classscheduler.dto.request.BookingRequest;
import com.example.classscheduler.dto.response.BookingResponse;
import com.example.classscheduler.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Enrollments & Bookings", description = "Endpoints for parents to purchase cohort courses and review histories")
@RestController
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Register enrollment for a course offering under strict concurrency locks")
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Query a parent's cohort booking calendar translated to their local timezone")
    @GetMapping("/parents/{parentId}/bookings")
    public ResponseEntity<List<BookingResponse>> getParentBookings(@PathVariable Long parentId) {
        List<BookingResponse> response = bookingService.getBookingsByParent(parentId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
