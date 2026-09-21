CREATE TABLE metals (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_metals_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_metals_company_code
        UNIQUE (company_id, code)
);

CREATE INDEX idx_metals_company_id
    ON metals(company_id);

CREATE INDEX idx_metals_active
    ON metals(active);