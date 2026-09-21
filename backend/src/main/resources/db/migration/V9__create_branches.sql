CREATE TABLE branches (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(100),
    country_code VARCHAR(2),
    phone VARCHAR(50),
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_branches_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_branches_company_code
        UNIQUE (company_id, code)
);

CREATE INDEX idx_branches_company_id
    ON branches(company_id);

CREATE INDEX idx_branches_active
    ON branches(active);