package com.kbl_dev.job_tracker.repository;

import com.kbl_dev.job_tracker.IntegrationTestBase;
import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.entity.ApplicationStatusHistory;
import com.kbl_dev.job_tracker.data.reference.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusHistoryRepositoryIT extends IntegrationTestBase {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationStatusHistoryRepository historyRepository;

    @Test
    void deletingApplication_cascadesToStatusHistory() {
        Application app = new Application();
        app.setOwnerSub("test-user");
        app.setCompany("Acme Corp");
        app.setTitle("Backend Engineer");
        app.setStatus(ApplicationStatus.APPLIED);
        app = applicationRepository.save(app);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(app.getId());
        history.setStatus(ApplicationStatus.APPLIED);
        historyRepository.save(history);

        applicationRepository.delete(app);
        applicationRepository.flush();

        List<ApplicationStatusHistory> remaining =
                historyRepository.findByApplicationIdOrderByChangedAtAsc(app.getId());

        assertThat(remaining).isEmpty();
    }
}
