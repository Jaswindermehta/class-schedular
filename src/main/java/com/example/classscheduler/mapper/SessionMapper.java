package com.example.classscheduler.mapper;

import com.example.classscheduler.dto.response.SessionResponse;
import com.example.classscheduler.entity.Session;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class SessionMapper {

    public SessionResponse toResponse(Session session) {
        return toResponseWithZone(session, ZoneOffset.UTC);
    }

    public SessionResponse toResponseWithZone(Session session, ZoneId targetZone) {
        if (session == null) {
            return null;
        }

        Long offeringId = session.getOffering() != null ? session.getOffering().getId() : null;

        // Project the UTC instant to the target zone and keep the local offset
        OffsetDateTime localizedStart = session.getStartTimeUtc().atZoneSameInstant(targetZone).toOffsetDateTime();
        OffsetDateTime localizedEnd = session.getEndTimeUtc().atZoneSameInstant(targetZone).toOffsetDateTime();

        return new SessionResponse(
            session.getId(),
            offeringId,
            localizedStart,
            localizedEnd,
            targetZone.getId()
        );
    }
}
