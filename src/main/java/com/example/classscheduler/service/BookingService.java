package com.example.classscheduler.service;

import com.example.classscheduler.dto.request.BookingRequest;
import com.example.classscheduler.dto.response.BookingResponse;
import com.example.classscheduler.dto.response.SessionResponse;
import com.example.classscheduler.entity.Booking;
import com.example.classscheduler.entity.Offering;
import com.example.classscheduler.entity.Parent;
import com.example.classscheduler.entity.Session;
import com.example.classscheduler.exception.ConflictException;
import com.example.classscheduler.exception.ResourceNotFoundException;
import com.example.classscheduler.mapper.BookingMapper;
import com.example.classscheduler.mapper.SessionMapper;
import com.example.classscheduler.repository.BookingRepository;
import com.example.classscheduler.repository.OfferingRepository;
import com.example.classscheduler.repository.ParentRepository;
import com.example.classscheduler.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ParentRepository parentRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final BookingMapper bookingMapper;
    private final SessionMapper sessionMapper;

    public BookingService(BookingRepository bookingRepository,
                          ParentRepository parentRepository,
                          OfferingRepository offeringRepository,
                          SessionRepository sessionRepository,
                          BookingMapper bookingMapper,
                          SessionMapper sessionMapper) {
        this.bookingRepository = bookingRepository;
        this.parentRepository = parentRepository;
        this.offeringRepository = offeringRepository;
        this.sessionRepository = sessionRepository;
        this.bookingMapper = bookingMapper;
        this.sessionMapper = sessionMapper;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Booking offering {} for parent {}", request.getOfferingId(), request.getParentId());

        // Lock Parent record to prevent concurrent booking conflicts
        Parent parent = parentRepository.findByIdForUpdate(request.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with ID: " + request.getParentId()));

        Offering offering = offeringRepository.findById(request.getOfferingId())
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found with ID: " + request.getOfferingId()));

        // Validate duplicates
        boolean alreadyBooked = bookingRepository.findAll().stream()
                .anyMatch(b -> b.getParent().getId().equals(parent.getId()) 
                        && b.getOffering().getId().equals(offering.getId()));
        if (alreadyBooked) {
            throw new ConflictException("Parent has already booked this offering");
        }

        // Fetch requested sessions
        List<Session> requestedSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getOffering().getId().equals(offering.getId()))
                .collect(Collectors.toList());

        // Fetch sessions of already booked offerings
        List<Booking> existingBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getParent().getId().equals(parent.getId()))
                .collect(Collectors.toList());

        List<Session> bookedSessions = existingBookings.stream()
                .flatMap(b -> sessionRepository.findAll().stream()
                        .filter(s -> s.getOffering().getId().equals(b.getOffering().getId())))
                .collect(Collectors.toList());

        // Overlap detection algorithm (Session Level)
        for (Session reqSession : requestedSessions) {
            for (Session existingSession : bookedSessions) {
                boolean overlaps = reqSession.getStartTimeUtc().isBefore(existingSession.getEndTimeUtc()) 
                        && reqSession.getEndTimeUtc().isAfter(existingSession.getStartTimeUtc());
                
                if (overlaps) {
                    throw new ConflictException(String.format(
                            "Schedule conflict: Session overlaps with session (ID: %d) in offering (ID: %d)",
                            existingSession.getId(), existingSession.getOffering().getId()
                    ));
                }
            }
        }

        Booking booking = new Booking();
        booking.setParent(parent);
        booking.setOffering(offering);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booked offering {} for parent {}", offering.getId(), parent.getId());

        // Map the sessions using the parent's target local timezone
        ZoneId parentZone = ZoneId.of(parent.getTimezone());
        List<SessionResponse> sessionResponses = requestedSessions.stream()
                .map(s -> sessionMapper.toResponseWithZone(s, parentZone))
                .collect(Collectors.toList());

        return bookingMapper.toResponseWithSessions(savedBooking, sessionResponses);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByParent(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with ID: " + parentId));

        ZoneId parentZone = ZoneId.of(parent.getTimezone());

        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getParent().getId().equals(parentId))
                .collect(Collectors.toList());

        return bookings.stream().map(booking -> {
            List<Session> sessions = sessionRepository.findAll().stream()
                    .filter(s -> s.getOffering().getId().equals(booking.getOffering().getId()))
                    .collect(Collectors.toList());

            List<SessionResponse> sessionResponses = sessions.stream()
                    .map(s -> sessionMapper.toResponseWithZone(s, parentZone))
                    .collect(Collectors.toList());

            return bookingMapper.toResponseWithSessions(booking, sessionResponses);
        }).collect(Collectors.toList());
    }
}
