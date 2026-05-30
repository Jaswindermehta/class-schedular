package com.example.classscheduler.service;

import com.example.classscheduler.dto.request.CreateOfferingRequest;
import com.example.classscheduler.dto.response.OfferingResponse;
import com.example.classscheduler.dto.response.SessionResponse;
import com.example.classscheduler.dto.response.TeacherOfferingResponse;
import com.example.classscheduler.entity.Course;
import com.example.classscheduler.entity.Offering;
import com.example.classscheduler.entity.Teacher;
import com.example.classscheduler.exception.BadRequestException;
import com.example.classscheduler.exception.ResourceNotFoundException;
import com.example.classscheduler.mapper.OfferingMapper;
import com.example.classscheduler.mapper.SessionMapper;
import com.example.classscheduler.repository.CourseRepository;
import com.example.classscheduler.repository.OfferingRepository;
import com.example.classscheduler.repository.SessionRepository;
import com.example.classscheduler.repository.TeacherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
    private final OfferingMapper offeringMapper;
    private final SessionMapper sessionMapper;

    public OfferingService(OfferingRepository offeringRepository,
                           CourseRepository courseRepository,
                           TeacherRepository teacherRepository,
                           SessionRepository sessionRepository,
                           OfferingMapper offeringMapper,
                           SessionMapper sessionMapper) {
        this.offeringRepository = offeringRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.sessionRepository = sessionRepository;
        this.offeringMapper = offeringMapper;
        this.sessionMapper = sessionMapper;
    }

    @Transactional
    public OfferingResponse createOffering(CreateOfferingRequest request) {
        log.info("Creating offering: {} for course: {} by teacher: {}", request.getName(), request.getCourseId(), request.getTeacherId());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + request.getTeacherId()));

        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid timezone ID: " + request.getTimezone());
        }

        Offering offering = new Offering();
        offering.setCourse(course);
        offering.setTeacher(teacher);
        offering.setName(request.getName());
        offering.setTimezone(request.getTimezone());

        Offering savedOffering = offeringRepository.save(offering);
        return offeringMapper.toResponse(savedOffering);
    }

    @Transactional(readOnly = true)
    public List<TeacherOfferingResponse> getOfferingsByTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        List<Offering> offerings = offeringRepository.findAll().stream()
                .filter(o -> o.getTeacher().getId().equals(teacherId))
                .collect(Collectors.toList());

        return offerings.stream().map(offering -> {
            ZoneId offeringZone = ZoneId.of(offering.getTimezone());
            List<SessionResponse> sessions = sessionRepository.findAll().stream()
                    .filter(s -> s.getOffering().getId().equals(offering.getId()))
                    .map(s -> sessionMapper.toResponseWithZone(s, offeringZone))
                    .collect(Collectors.toList());

            return new TeacherOfferingResponse(
                    offering.getId(),
                    offering.getName(),
                    offering.getTimezone(),
                    offering.getCourse().getId(),
                    offering.getCourse().getTitle(),
                    offering.getCreatedAt(),
                    sessions
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherOfferingResponse> getAllOfferings(ZoneId targetZone) {
        log.info("Fetching all offerings with timezone display: {}", targetZone.getId());
        List<Offering> offerings = offeringRepository.findAllWithCourseAndTeacher();

        return offerings.stream().map(offering -> {
            List<SessionResponse> sessions = sessionRepository.findAll().stream()
                    .filter(s -> s.getOffering().getId().equals(offering.getId()))
                    .map(s -> sessionMapper.toResponseWithZone(s, targetZone))
                    .collect(Collectors.toList());

            return new TeacherOfferingResponse(
                    offering.getId(),
                    offering.getName(),
                    offering.getTimezone(),
                    offering.getCourse().getId(),
                    offering.getCourse().getTitle(),
                    offering.getCreatedAt(),
                    sessions
            );
        }).collect(Collectors.toList());
    }
}
