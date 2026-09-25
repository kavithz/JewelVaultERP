CREATE TABLE expense_categories (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_expense_categories_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT uk_expense_categories_company_name
        UNIQUE (company_id, name)
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    branch_id UUID,
    category_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    expense_date TIMESTAMP NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    reference_number VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_expenses_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT fk_expenses_branch
        FOREIGN KEY (branch_id) REFERENCES branches(id),

    CONSTRAINT fk_expenses_category
        FOREIGN KEY (category_id) REFERENCES expense_categories(id),

    CONSTRAINT chk_expenses_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_expense_categories_company_id
    ON expense_categories(company_id);

CREATE INDEX idx_expenses_company_id
    ON expenses(company_id);

CREATE INDEX idx_expenses_branch_id
    ON expenses(branch_id);

CREATE INDEX idx_expenses_category_id
    ON expenses(category_id);

CREATE INDEX idx_expenses_status
    ON expenses(status);

CREATE INDEX idx_expenses_expense_date
    ON expenses(expense_date);