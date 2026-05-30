package com.example.classscheduler.service;

import com.example.classscheduler.dto.request.CreateParentRequest;
import com.example.classscheduler.dto.response.ParentResponse;
import com.example.classscheduler.entity.Parent;
import com.example.classscheduler.exception.BadRequestException;
import com.example.classscheduler.exception.ConflictException;
import com.example.classscheduler.mapper.ParentMapper;
import com.example.classscheduler.repository.ParentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Slf4j
@Service
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper parentMapper;

    public ParentService(ParentRepository parentRepository, ParentMapper parentMapper) {
        this.parentRepository = parentRepository;
        this.parentMapper = parentMapper;
    }

    @Transactional
    public ParentResponse createParent(CreateParentRequest request) {
        log.info("Creating parent with email: {}", request.getEmail());
        // Validate unique email
        parentRepository.findAll().stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(request.getEmail()))
                .findAny()
                .ifPresent(p -> {
                    throw new ConflictException("Email is already registered by another parent");
                });

        // Validate timezone structure
        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid timezone ID: " + request.getTimezone());
        }

        Parent parent = new Parent();
        parent.setName(request.getName());
        parent.setEmail(request.getEmail());
        parent.setTimezone(request.getTimezone());

        Parent savedParent = parentRepository.save(parent);
        return parentMapper.toResponse(savedParent);
    }
}
