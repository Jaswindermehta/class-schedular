package com.example.classscheduler.service;

import com.example.classscheduler.dto.request.CreateTeacherRequest;
import com.example.classscheduler.dto.response.TeacherResponse;
import com.example.classscheduler.entity.Teacher;
import com.example.classscheduler.exception.BadRequestException;
import com.example.classscheduler.exception.ConflictException;
import com.example.classscheduler.mapper.TeacherMapper;
import com.example.classscheduler.repository.TeacherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Slf4j
@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    public TeacherService(TeacherRepository teacherRepository, TeacherMapper teacherMapper) {
        this.teacherRepository = teacherRepository;
        this.teacherMapper = teacherMapper;
    }

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        log.info("Creating teacher with email: {}", request.getEmail());
        // Validate unique email
        teacherRepository.findAll().stream()
                .filter(t -> t.getEmail().equalsIgnoreCase(request.getEmail()))
                .findAny()
                .ifPresent(t -> {
                    throw new ConflictException("Email is already registered by another teacher");
                });

        // Validate timezone structure
        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid timezone ID: " + request.getTimezone());
        }

        Teacher teacher = new Teacher();
        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setTimezone(request.getTimezone());

        Teacher savedTeacher = teacherRepository.save(teacher);
        return teacherMapper.toResponse(savedTeacher);
    }
}
