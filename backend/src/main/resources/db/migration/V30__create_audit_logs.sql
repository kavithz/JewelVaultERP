CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(30) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    description VARCHAR(1000),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_audit_logs_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_company_id
    ON audit_logs(company_id);

CREATE INDEX idx_audit_logs_user_id
    ON audit_logs(user_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action);

CREATE INDEX idx_audit_logs_entity_type
    ON audit_logs(entity_type);

CREATE INDEX idx_audit_logs_entity_id
    ON audit_logs(entity_id);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at);
