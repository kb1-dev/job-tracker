CREATE TABLE application_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    note TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_application_status_history_application
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX idx_application_status_history_application_id ON application_status_history(application_id);