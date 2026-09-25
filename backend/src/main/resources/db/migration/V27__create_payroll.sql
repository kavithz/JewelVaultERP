CREATE TABLE payroll (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    payroll_year INTEGER NOT NULL,
    payroll_month INTEGER NOT NULL,

    basic_salary NUMERIC(14,2) NOT NULL,
    allowances NUMERIC(14,2) NOT NULL DEFAULT 0,
    deductions NUMERIC(14,2) NOT NULL DEFAULT 0,
    gross_salary NUMERIC(14,2) NOT NULL,
    net_salary NUMERIC(14,2) NOT NULL,

    payment_date DATE,
    payment_method VARCHAR(30),
    reference_number VARCHAR(100),

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_payroll_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id),

    CONSTRAINT uk_payroll_employee_period
        UNIQUE (employee_id, payroll_year, payroll_month),

    CONSTRAINT chk_payroll_month
        CHECK (payroll_month BETWEEN 1 AND 12),

    CONSTRAINT chk_payroll_basic_salary
        CHECK (basic_salary >= 0),

    CONSTRAINT chk_payroll_allowances
        CHECK (allowances >= 0),

    CONSTRAINT chk_payroll_deductions
        CHECK (deductions >= 0),

    CONSTRAINT chk_payroll_gross_salary
        CHECK (gross_salary >= 0),

    CONSTRAINT chk_payroll_net_salary
        CHECK (net_salary >= 0)
);

CREATE INDEX idx_payroll_company_id
    ON payroll(company_id);

CREATE INDEX idx_payroll_employee_id
    ON payroll(employee_id);

CREATE INDEX idx_payroll_status
    ON payroll(status);

CREATE INDEX idx_payroll_year_month
    ON payroll(payroll_year, payroll_month);