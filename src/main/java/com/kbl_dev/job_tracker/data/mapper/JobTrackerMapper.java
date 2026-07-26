package com.kbl_dev.job_tracker.data.mapper;

import com.kbl_dev.job_tracker.api.model.*;
import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.entity.ApplicationStatusHistory;
import com.kbl_dev.job_tracker.data.entity.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface JobTrackerMapper {

    // MapStruct doesn't ship a default Instant -> OffsetDateTime conversion (it needs a
    // zone to attach), so this fills that gap. Once defined here, MapStruct picks it up
    // automatically anywhere it needs to convert between these two types.
    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }

    ApplicationDto toDto(Application entity);

    List<ApplicationDto> toDtoList(List<Application> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownerSub", source = "ownerSub")
    Application toEntity(ApplicationDto dto, String ownerSub);

    default ApplicationPage toPage(Page<Application> page) {
        ApplicationPage result = new ApplicationPage();
        result.setContent(toDtoList(page.getContent()));
        result.setPage(page.getNumber());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        return result;
    }

    ApplicationStatusHistoryDto toDto(ApplicationStatusHistory entity);
    List<ApplicationStatusHistoryDto> toStatusHistoryDtoList(List<ApplicationStatusHistory> entities);

    InterviewDto toDto(Interview interview);
    List<InterviewDto> toInterviewDtoList(List<Interview> interviews);

    @Mapping(target = "applicationId", source = "applicationId")
    Interview toEntity(InterviewCreateDto dto, UUID applicationId);

    @Mapping(target = "id", ignore = true)
    void updateEntity(InterviewUpdateDto dto, @MappingTarget Interview entity);
}

