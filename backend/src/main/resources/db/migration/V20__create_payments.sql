CREATE TABLE payments (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    payment_number VARCHAR(100) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    reference_number VARCHAR(100),
    notes VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    customer_id UUID,
    supplier_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_payments_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_payments_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_payments_supplier
        FOREIGN KEY (supplier_id)
        REFERENCES suppliers(id),

    CONSTRAINT uk_payments_company_number
        UNIQUE (company_id, payment_number),

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_payments_company_id
    ON payments(company_id);

CREATE INDEX idx_payments_customer_id
    ON payments(customer_id);

CREATE INDEX idx_payments_supplier_id
    ON payments(supplier_id);

CREATE INDEX idx_payments_payment_date
    ON payments(payment_date);