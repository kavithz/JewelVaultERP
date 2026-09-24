CREATE TABLE receivables (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    invoice_id UUID,
    amount NUMERIC(14,2) NOT NULL,
    paid_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    outstanding_amount NUMERIC(14,2) NOT NULL,
    due_date TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_receivables_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT fk_receivables_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT fk_receivables_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id),

    CONSTRAINT chk_receivables_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_receivables_paid_amount
        CHECK (paid_amount >= 0),

    CONSTRAINT chk_receivables_outstanding_amount
        CHECK (outstanding_amount >= 0),

    CONSTRAINT chk_receivables_paid_not_over_amount
        CHECK (paid_amount <= amount)
);

CREATE TABLE payables (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    supplier_id UUID NOT NULL,
    invoice_id UUID,
    amount NUMERIC(14,2) NOT NULL,
    paid_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    outstanding_amount NUMERIC(14,2) NOT NULL,
    due_date TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_payables_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT fk_payables_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(id),

    CONSTRAINT fk_payables_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id),

    CONSTRAINT chk_payables_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payables_paid_amount
        CHECK (paid_amount >= 0),

    CONSTRAINT chk_payables_outstanding_amount
        CHECK (outstanding_amount >= 0),

    CONSTRAINT chk_payables_paid_not_over_amount
        CHECK (paid_amount <= amount)
);

CREATE INDEX idx_receivables_company_id
    ON receivables(company_id);

CREATE INDEX idx_receivables_customer_id
    ON receivables(customer_id);

CREATE INDEX idx_receivables_status
    ON receivables(status);

CREATE INDEX idx_payables_company_id
    ON payables(company_id);

CREATE INDEX idx_payables_supplier_id
    ON payables(supplier_id);

CREATE INDEX idx_payables_status
    ON payables(status);