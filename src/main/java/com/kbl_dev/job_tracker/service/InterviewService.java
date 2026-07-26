package com.kbl_dev.job_tracker.service;

import com.kbl_dev.job_tracker.api.InterviewsApiDelegate;
import com.kbl_dev.job_tracker.api.model.InterviewCreateDto;
import com.kbl_dev.job_tracker.api.model.InterviewDto;
import com.kbl_dev.job_tracker.api.model.InterviewUpdateDto;
import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.entity.Interview;
import com.kbl_dev.job_tracker.data.mapper.JobTrackerMapper;
import com.kbl_dev.job_tracker.data.reference.InterviewOutcome;
import com.kbl_dev.job_tracker.repository.ApplicationRepository;
import com.kbl_dev.job_tracker.repository.InterviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService implements InterviewsApiDelegate {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final JobTrackerMapper mapper;

    @Override
    @Transactional
    public ResponseEntity<InterviewDto> create(UUID applicationId, InterviewCreateDto dto) {
        Application application = applicationRepository.findById(applicationId  )
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        // ownership check — mirrors the pattern you'll want once security is wired in
        if (!application.getOwnerSub().equals(currentOwnerSub())) {
            throw new EntityNotFoundException("Application not found");
        }

        Interview entity = mapper.toEntity(dto, applicationId);
        entity.setOutcome(InterviewOutcome.PENDING);
        Interview saved = interviewRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<InterviewDto>> callList(UUID applicationId) {
        List<Interview> interviews = interviewRepository.findByApplicationIdOrderByScheduledAtAsc(applicationId);
        return ResponseEntity.ok(mapper.toInterviewDtoList(interviews));
    }

    @Override
    @Transactional
    public ResponseEntity<InterviewDto> update(UUID interviewId, InterviewUpdateDto dto) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

        Application application = applicationRepository.findById(interview.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        if (!application.getOwnerSub().equals(currentOwnerSub())) {
            throw new EntityNotFoundException("Interview not found");
        }

        mapper.updateEntity(dto, interview);

        return ResponseEntity.ok(mapper.toDto(interview));
    }

    private String currentOwnerSub() {
        return "temp-dev-user"; // same TODO as ApplicationService
    }
}
