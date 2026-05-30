package com.example.classscheduler.service;

import com.example.classscheduler.dto.request.AddSessionRequest;
import com.example.classscheduler.dto.response.SessionResponse;
import com.example.classscheduler.entity.Offering;
import com.example.classscheduler.entity.Session;
import com.example.classscheduler.exception.BadRequestException;
import com.example.classscheduler.exception.ConflictException;
import com.example.classscheduler.exception.ResourceNotFoundException;
import com.example.classscheduler.mapper.SessionMapper;
import com.example.classscheduler.repository.OfferingRepository;
import com.example.classscheduler.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final OfferingRepository offeringRepository;
    private final SessionMapper sessionMapper;

    public SessionService(SessionRepository sessionRepository,
                          OfferingRepository offeringRepository,
                          SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.offeringRepository = offeringRepository;
        this.sessionMapper = sessionMapper;
    }

    @Transactional
    public List<SessionResponse> addSessions(Long offeringId, List<AddSessionRequest> requests) {
        log.info("Adding sessions for offering ID: {}", offeringId);

        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found with ID: " + offeringId));

        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Session request list cannot be empty");
        }

        ZoneId offeringZone = ZoneId.of(offering.getTimezone());
        List<Session> sessionsToSave = new ArrayList<>();
        
        // Track unique session ranges in the incoming batch to prevent request duplicates
        Set<String> batchRanges = new HashSet<>();

        // Retrieve existing sessions for this offering to prevent DB duplicates
        List<Session> existingSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getOffering().getId().equals(offeringId))
                .collect(Collectors.toList());

        for (AddSessionRequest request : requests) {
            LocalDateTime localStart = request.getStartTime();
            LocalDateTime localEnd = request.getEndTime();

            if (localStart == null || localEnd == null) {
                throw new BadRequestException("Session start and end times are required");
            }

            if (!localEnd.isAfter(localStart)) {
                throw new BadRequestException("Session end time must be after the start time");
            }

            // Convert raw LocalDateTime to offering-specific ZonedDateTime
            ZonedDateTime zonedStart = ZonedDateTime.of(localStart, offeringZone);
            ZonedDateTime zonedEnd = ZonedDateTime.of(localEnd, offeringZone);

            // Convert ZonedDateTime to UTC OffsetDateTime
            OffsetDateTime startTimeUtc = zonedStart.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
            OffsetDateTime endTimeUtc = zonedEnd.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);

            // Check duplicate inside the batch request list
            String rangeKey = startTimeUtc.toString() + "_" + endTimeUtc.toString();
            if (!batchRanges.add(rangeKey)) {
                throw new ConflictException("Duplicate session time range found in request batch: " + localStart + " to " + localEnd);
            }

            // Check duplicate against already existing database sessions for this offering
            boolean duplicateExists = existingSessions.stream()
                    .anyMatch(s -> s.getStartTimeUtc().equals(startTimeUtc) && s.getEndTimeUtc().equals(endTimeUtc));
            if (duplicateExists) {
                throw new ConflictException("Session time range already registered for this offering: " + localStart + " to " + localEnd);
            }

            Session session = new Session();
            session.setOffering(offering);
            session.setStartTimeUtc(startTimeUtc);
            session.setEndTimeUtc(endTimeUtc);

            sessionsToSave.add(session);
        }

        List<Session> savedSessions = sessionRepository.saveAll(sessionsToSave);
        log.info("Created {} sessions for offering {}", savedSessions.size(), offeringId);

        return savedSessions.stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }
}
