CREATE TABLE customers (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    address VARCHAR(255),
    tax_number VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_customers_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_customers_company_code
        UNIQUE (company_id, code)
);

CREATE INDEX idx_customers_company_id
    ON customers(company_id);

CREATE INDEX idx_customers_active
    ON customers(active);