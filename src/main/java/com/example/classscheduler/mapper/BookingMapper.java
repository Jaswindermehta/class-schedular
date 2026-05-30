package com.example.classscheduler.mapper;

import com.example.classscheduler.dto.response.BookingResponse;
import com.example.classscheduler.dto.response.SessionResponse;
import com.example.classscheduler.entity.Booking;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return toResponseWithSessions(booking, Collections.emptyList());
    }

    public BookingResponse toResponseWithSessions(Booking booking, List<SessionResponse> sessions) {
        if (booking == null) {
            return null;
        }

        Long parentId = booking.getParent() != null ? booking.getParent().getId() : null;
        String parentName = booking.getParent() != null ? booking.getParent().getName() : null;
        
        Long offeringId = booking.getOffering() != null ? booking.getOffering().getId() : null;
        String offeringName = booking.getOffering() != null ? booking.getOffering().getName() : null;

        return new BookingResponse(
            booking.getId(),
            parentId,
            parentName,
            offeringId,
            offeringName,
            booking.getBookedAt(),
            sessions
        );
    }
}
