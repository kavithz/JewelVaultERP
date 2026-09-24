CREATE TABLE taxes (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    tax_type VARCHAR(30) NOT NULL,
    rate NUMERIC(8,4) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_taxes_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT uk_taxes_company_code
        UNIQUE (company_id, code),

    CONSTRAINT chk_taxes_rate
        CHECK (rate >= 0)
);

CREATE INDEX idx_taxes_company_id
    ON taxes(company_id);

CREATE INDEX idx_taxes_type
    ON taxes(tax_type);

CREATE INDEX idx_taxes_active
    ON taxes(active);