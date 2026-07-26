package com.kbl_dev.job_tracker.repository;

import com.kbl_dev.job_tracker.data.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    List<Interview> findByApplicationIdOrderByScheduledAtAsc(UUID applicationId);
}
