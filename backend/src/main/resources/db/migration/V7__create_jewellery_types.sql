CREATE TABLE jewellery_types (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_jewellery_types_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_jewellery_types_company_code
        UNIQUE (company_id, code)
);

CREATE INDEX idx_jewellery_types_company_id
    ON jewellery_types(company_id);

CREATE INDEX idx_jewellery_types_active
    ON jewellery_types(active);