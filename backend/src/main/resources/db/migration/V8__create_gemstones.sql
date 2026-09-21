CREATE TABLE gemstones (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    color VARCHAR(100),
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_gemstones_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_gemstones_company_code
        UNIQUE (company_id, code)
);

CREATE INDEX idx_gemstones_company_id
    ON gemstones(company_id);

CREATE INDEX idx_gemstones_active
    ON gemstones(active);