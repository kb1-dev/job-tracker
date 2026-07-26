package com.kbl_dev.job_tracker.repository;

import com.kbl_dev.job_tracker.data.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {
    List<ApplicationStatusHistory> findByApplicationIdOrderByChangedAtAsc(UUID applicationId);
}
