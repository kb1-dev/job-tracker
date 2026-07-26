package com.kbl_dev.job_tracker.service;

import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.entity.Interview;
import com.kbl_dev.job_tracker.data.mapper.JobTrackerMapper;
import com.kbl_dev.job_tracker.repository.ApplicationRepository;
import com.kbl_dev.job_tracker.repository.InterviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobTrackerMapper mapper;

    @InjectMocks
    private InterviewService interviewService;

    @Test
    void create_throwsWhenApplicationNotOwnedByCurrentUser() {
        UUID applicationId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setOwnerSub("someone-else");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        var dto = new com.kbl_dev.job_tracker.api.model.InterviewCreateDto();
        dto.setRound("Phone Screen");

        assertThatThrownBy(() -> interviewService.create(applicationId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Application not found");

        verifyNoInteractions(interviewRepository);
    }

    @Test
    void create_throwsWhenApplicationDoesNotExist() {
        UUID applicationId = UUID.randomUUID();
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        var dto = new com.kbl_dev.job_tracker.api.model.InterviewCreateDto();

        assertThatThrownBy(() -> interviewService.create(applicationId, dto))
                .isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(interviewRepository);
    }

    @Test
    void update_throwsWhenApplicationNotOwnedByCurrentUser() {
        UUID interviewId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setApplicationId(applicationId);

        Application application = new Application();
        application.setId(applicationId);
        application.setOwnerSub("someone-else");

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        var dto = new com.kbl_dev.job_tracker.api.model.InterviewUpdateDto();

        assertThatThrownBy(() -> interviewService.update(interviewId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Interview not found");

        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_throwsWhenInterviewDoesNotExist() {
        UUID interviewId = UUID.randomUUID();
        when(interviewRepository.findById(interviewId)).thenReturn(Optional.empty());

        var dto = new com.kbl_dev.job_tracker.api.model.InterviewUpdateDto();

        assertThatThrownBy(() -> interviewService.update(interviewId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Interview not found");
    }

    @Test
    void create_succeedsWhenOwnedCorrectly() {
        UUID applicationId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setOwnerSub("temp-dev-user"); // matches currentOwnerSub() stub

        var dto = new com.kbl_dev.job_tracker.api.model.InterviewCreateDto();
        dto.setRound("Phone Screen");

        Interview mappedEntity = new Interview();
        mappedEntity.setRound("Phone Screen");

        Interview savedEntity = new Interview();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setRound("Phone Screen");

        var expectedDto = new com.kbl_dev.job_tracker.api.model.InterviewDto();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(mapper.toEntity(dto, applicationId)).thenReturn(mappedEntity);
        when(interviewRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(expectedDto);

        var response = interviewService.create(applicationId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expectedDto);
        verify(interviewRepository).save(mappedEntity);
    }

    @Test
    void update_succeedsWhenOwnedCorrectly() {
        UUID interviewId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setApplicationId(applicationId);

        Application application = new Application();
        application.setId(applicationId);
        application.setOwnerSub("temp-dev-user");

        var dto = new com.kbl_dev.job_tracker.api.model.InterviewUpdateDto();
        dto.setNotes("Went well");

        var expectedDto = new com.kbl_dev.job_tracker.api.model.InterviewDto();

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(mapper.toDto(interview)).thenReturn(expectedDto);

        var response = interviewService.update(interviewId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedDto);
        verify(mapper).updateEntity(dto, interview);
    }
}