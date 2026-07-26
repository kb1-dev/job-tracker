package com.kbl_dev.job_tracker.data.entity;

import com.kbl_dev.job_tracker.data.reference.InterviewOutcome;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private String round; // e.g. "Phone Screen", "Onsite", "Final"

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "interviewer_name")
    private String interviewerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterviewOutcome outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
