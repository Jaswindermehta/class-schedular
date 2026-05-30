package com.example.classscheduler;

import com.example.classscheduler.dto.request.*;
import com.example.classscheduler.dto.response.*;
import com.example.classscheduler.exception.ConflictException;
import com.example.classscheduler.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingIntegrationTest {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ParentService parentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private OfferingService offeringService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BookingService bookingService;

    @Test
    void testCompleteBookingOverlapAndTimezoneFlow() {
        // 1. Create a Teacher residing in New York timezone
        CreateTeacherRequest teacherReq = new CreateTeacherRequest("Sarah Miller", "sarah@example.com", "America/New_York");
        TeacherResponse teacher = teacherService.createTeacher(teacherReq);
        assertNotNull(teacher.getId());

        // 2. Create a Course catalog entry
        CreateCourseRequest courseReq = new CreateCourseRequest("Intro to Java Programming", "Learn core Java concepts.");
        CourseResponse course = courseService.createCourse(courseReq);
        assertNotNull(course.getId());

        // 3. Create Offering A taught by Sarah in New York timezone
        CreateOfferingRequest offeringReqA = new CreateOfferingRequest(course.getId(), teacher.getId(), "Intro Java - Mon Cohort", "America/New_York");
        OfferingResponse offeringA = offeringService.createOffering(offeringReqA);
        assertNotNull(offeringA.getId());

        // 4. Create Sessions in bulk for Offering A (Mondays: 9:00 AM - 10:30 AM in New York timezone)
        // Let's specify LocalDateTime ranges representing Sarah's local wall-clock times
        LocalDateTime session1Start = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime session1End = LocalDateTime.of(2026, 6, 1, 10, 30);
        
        AddSessionRequest sessionReqA = new AddSessionRequest(session1Start, session1End);
        List<SessionResponse> sessionsA = sessionService.addSessions(offeringA.getId(), Arrays.asList(sessionReqA));
        assertEquals(1, sessionsA.size());
        
        // Assert stored UTC time is projected correctly (New York in June is DST, UTC-4)
        // 9:00 AM NY time corresponds to 1:00 PM UTC (13:00)
        OffsetDateTime expectedStartUtc = session1Start.atZone(ZoneId.of("America/New_York")).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        assertEquals(expectedStartUtc, sessionsA.get(0).getStartTime());

        // 5. Create a Parent residing in London timezone (which uses British Summer Time in June, UTC+1)
        CreateParentRequest parentReq = new CreateParentRequest("John Watson", "john@example.com", "Europe/London");
        ParentResponse parent = parentService.createParent(parentReq);
        assertNotNull(parent.getId());

        // 6. Parent Books Offering A successfully
        BookingRequest bookingReqA = new BookingRequest(parent.getId(), offeringA.getId());
        BookingResponse bookingA = bookingService.createBooking(bookingReqA);
        assertNotNull(bookingA.getId());
        assertEquals(1, bookingA.getSessions().size());

        // Verify parent timezone presentation conversion:
        // UTC 13:00 (expectedStartUtc) maps to 14:00 (2:00 PM) in Europe/London in June
        OffsetDateTime displayStart = bookingA.getSessions().get(0).getStartTime();
        assertEquals(14, displayStart.getHour());
        assertEquals("Europe/London", bookingA.getSessions().get(0).getTimezone());

        // 7. Create Offering B (a different class cohort)
        CreateOfferingRequest offeringReqB = new CreateOfferingRequest(course.getId(), teacher.getId(), "Intro Java - Mon Alternate", "America/New_York");
        OfferingResponse offeringB = offeringService.createOffering(offeringReqB);
        
        // 8. Create a Session for Offering B that overlaps (9:30 AM - 11:00 AM NY time on the same day)
        // Since offering A is 9:00 - 10:30 and B is 9:30 - 11:00, they overlap at 9:30-10:30 NY time
        LocalDateTime session2Start = LocalDateTime.of(2026, 6, 1, 9, 30);
        LocalDateTime session2End = LocalDateTime.of(2026, 6, 1, 11, 0);
        AddSessionRequest sessionReqB = new AddSessionRequest(session2Start, session2End);
        sessionService.addSessions(offeringB.getId(), Arrays.asList(sessionReqB));

        // 9. Parent attempts to book Offering B -> Must fail with ConflictException due to overlap
        BookingRequest bookingReqB = new BookingRequest(parent.getId(), offeringB.getId());
        assertThrows(ConflictException.class, () -> bookingService.createBooking(bookingReqB),
                "Expected overlap detection algorithm to trigger ConflictException");
    }
}
