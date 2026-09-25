CREATE TABLE employees (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    branch_id UUID,
    employee_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(255),
    department VARCHAR(100),
    designation VARCHAR(100),
    employment_status VARCHAR(30) NOT NULL,
    joining_date DATE NOT NULL,
    exit_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_employees_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT fk_employees_branch
        FOREIGN KEY (branch_id) REFERENCES branches(id),

    CONSTRAINT uk_employees_company_number
        UNIQUE (company_id, employee_number)
);

CREATE INDEX idx_employees_company_id
    ON employees(company_id);

CREATE INDEX idx_employees_branch_id
    ON employees(branch_id);

CREATE INDEX idx_employees_status
    ON employees(employment_status);

CREATE INDEX idx_employees_joining_date
    ON employees(joining_date);