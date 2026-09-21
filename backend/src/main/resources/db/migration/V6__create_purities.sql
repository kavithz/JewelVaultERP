CREATE TABLE purities (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    fineness NUMERIC(6,3) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_purities_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_purities_company_code
        UNIQUE (company_id, code)
);

CREATE INDEX idx_purities_company_id
    ON purities(company_id);

CREATE INDEX idx_purities_active
    ON purities(active);