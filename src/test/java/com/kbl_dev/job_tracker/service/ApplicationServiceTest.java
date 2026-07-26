package com.kbl_dev.job_tracker.service;

import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.entity.ApplicationStatusHistory;
import com.kbl_dev.job_tracker.data.mapper.JobTrackerMapper;
import com.kbl_dev.job_tracker.data.reference.ApplicationStatus;
import com.kbl_dev.job_tracker.repository.ApplicationRepository;
import com.kbl_dev.job_tracker.repository.ApplicationStatusHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    @Mock
    private JobTrackerMapper mapper;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void updateStatus_throwsOnNoOpTransition() {
        UUID id = UUID.randomUUID();
        Application app = new Application();
        app.setId(id);
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(id)).thenReturn(Optional.of(app));

        var dto = new com.kbl_dev.job_tracker.api.model.ApplicationStatusUpdateDto();
        dto.setStatus(com.kbl_dev.job_tracker.api.model.ApplicationStatus.APPLIED);

        assertThatThrownBy(() -> applicationService.updateStatus(id, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already in status");

        verifyNoInteractions(historyRepository);
    }

    @Test
    void updateStatus_writesHistoryOnRealTransition() {
        UUID id = UUID.randomUUID();
        Application app = new Application();
        app.setId(id);
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(id)).thenReturn(Optional.of(app));
        when(mapper.toDto(app)).thenReturn(new com.kbl_dev.job_tracker.api.model.ApplicationDto());

        var dto = new com.kbl_dev.job_tracker.api.model.ApplicationStatusUpdateDto();
        dto.setStatus(com.kbl_dev.job_tracker.api.model.ApplicationStatus.PHONE_SCREEN);
        dto.setNote("Recruiter called");

        applicationService.updateStatus(id, dto);

        verify(historyRepository).save(argThat(history ->
                history.getApplicationId().equals(id)
                        && history.getStatus() == ApplicationStatus.PHONE_SCREEN
                        && history.getNote().equals("Recruiter called")
        ));
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.PHONE_SCREEN);
    }

    @Test
    void create_persistsWithAppliedStatusByDefault() {
        var dto = new com.kbl_dev.job_tracker.api.model.ApplicationDto();
        dto.setCompany("Acme Corp");
        dto.setTitle("Backend Engineer");

        Application mappedEntity = new Application();
        mappedEntity.setCompany("Acme Corp");
        mappedEntity.setTitle("Backend Engineer");

        Application savedEntity = new Application();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setCompany("Acme Corp");
        savedEntity.setStatus(ApplicationStatus.APPLIED);

        var expectedDto = new com.kbl_dev.job_tracker.api.model.ApplicationDto();

        when(mapper.toEntity(dto, "temp-dev-user")).thenReturn(mappedEntity);
        when(applicationRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(expectedDto);

        var response = applicationService.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expectedDto);
        assertThat(mappedEntity.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        verify(applicationRepository).save(mappedEntity);
    }

    @Test
    void getStatusHistory_returnsHistoryOrderedByChangedAt() {
        UUID applicationId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);

        ApplicationStatusHistory historyEntry = new ApplicationStatusHistory();
        historyEntry.setApplicationId(applicationId);
        historyEntry.setStatus(ApplicationStatus.PHONE_SCREEN);

        var expectedDtoList = List.of(new com.kbl_dev.job_tracker.api.model.ApplicationStatusHistoryDto());

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(historyRepository.findByApplicationIdOrderByChangedAtAsc(applicationId))
                .thenReturn(List.of(historyEntry));
        when(mapper.toStatusHistoryDtoList(List.of(historyEntry))).thenReturn(expectedDtoList);

        var response = applicationService.getStatusHistory(applicationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedDtoList);
    }

    @Test
    void getStatusHistory_throwsWhenApplicationDoesNotExist() {
        UUID applicationId = UUID.randomUUID();
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getStatusHistory(applicationId))
                .isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(historyRepository);
    }
}
