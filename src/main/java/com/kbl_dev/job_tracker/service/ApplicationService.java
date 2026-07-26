package com.kbl_dev.job_tracker.service;

import com.kbl_dev.job_tracker.api.ApplicationsApiDelegate;
import com.kbl_dev.job_tracker.api.model.*;
import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.entity.ApplicationStatusHistory;
import com.kbl_dev.job_tracker.data.reference.ApplicationStatus;
import com.kbl_dev.job_tracker.data.mapper.JobTrackerMapper;
import com.kbl_dev.job_tracker.repository.ApplicationRepository;
import com.kbl_dev.job_tracker.repository.ApplicationSpecifications;
import com.kbl_dev.job_tracker.repository.ApplicationStatusHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService implements ApplicationsApiDelegate {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final JobTrackerMapper mapper;

    @Override
    @Transactional
    public ResponseEntity<ApplicationDto> create(ApplicationDto applicationDto) {
        Application entity = mapper.toEntity(applicationDto, currentOwnerSub());
        entity.setStatus(ApplicationStatus.APPLIED);
        Application saved = applicationRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApplicationPage> callList(Pageable pageable, ApplicationFilterDto applicationFilterDto) {
        List<ApplicationStatus> statuses = (applicationFilterDto == null || applicationFilterDto.getStatus() == null)
                ? null
                : applicationFilterDto.getStatus().stream()
                .map(s -> ApplicationStatus.valueOf(s.name()))
                .collect(Collectors.toList());

        Specification<Application> spec = Specification.allOf(
                ApplicationSpecifications.ownedBy(currentOwnerSub()),
                ApplicationSpecifications.hasStatusIn(statuses),
                ApplicationSpecifications.companyContains(applicationFilterDto == null ? null : applicationFilterDto.getCompany()),
                ApplicationSpecifications.appliedDateFrom(applicationFilterDto == null ? null : applicationFilterDto.getAppliedDateFrom()),
                ApplicationSpecifications.appliedDateTo(applicationFilterDto == null ? null : applicationFilterDto.getAppliedDateTo())
        );

        Page<Application> result = applicationRepository.findAll(spec, pageable);

        return ResponseEntity.ok(mapper.toPage(result));
    }

    // TODO: replace with the real Cognito JWT subject (jwt.getSubject()) once security is wired in
    private String currentOwnerSub() {
        return "temp-dev-user";
    }

    @Override
    @Transactional
    public ResponseEntity<ApplicationDto> updateStatus(UUID id, ApplicationStatusUpdateDto dto) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        ApplicationStatus status = ApplicationStatus.valueOf(dto.getStatus().name());

        if (application.getStatus() == status) {
            throw new IllegalStateException("Application is already in status " + dto.getStatus());
        }

        application.setStatus(status);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(id);
        history.setStatus(status);
        history.setNote(dto.getNote());

        applicationStatusHistoryRepository.save(history);

        return ResponseEntity.ok(mapper.toDto(application));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<ApplicationStatusHistoryDto>> getStatusHistory(UUID id) {
        applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        List<ApplicationStatusHistory> history =
                applicationStatusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(id);

        return ResponseEntity.ok(mapper.toStatusHistoryDtoList(history));
    }
}