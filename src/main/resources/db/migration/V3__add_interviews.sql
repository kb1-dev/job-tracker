CREATE TABLE interviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    round VARCHAR(100) NOT NULL,
    scheduled_at TIMESTAMP,
    interviewer_name VARCHAR(255),
    outcome VARCHAR(30) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_interviews_application
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX idx_interviews_application_id ON interviews(application_id);