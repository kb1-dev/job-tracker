package com.kbl_dev.job_tracker.repository;

import com.kbl_dev.job_tracker.data.entity.Application;
import com.kbl_dev.job_tracker.data.reference.ApplicationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collection;

public final class ApplicationSpecifications {

    private ApplicationSpecifications() {
    }

    public static Specification<Application> ownedBy(String ownerSub) {
        return (root, query, cb) -> cb.equal(root.get("ownerSub"), ownerSub);
    }

    public static Specification<Application> hasStatusIn(Collection<ApplicationStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Application> companyContains(String company) {
        return (root, query, cb) -> {
            if (company == null || company.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("company")),
                    "%" + company.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Application> appliedDateFrom(LocalDate from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("appliedDate"), from);
        };
    }

    public static Specification<Application> appliedDateTo(LocalDate to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("appliedDate"), to);
        };
    }
}