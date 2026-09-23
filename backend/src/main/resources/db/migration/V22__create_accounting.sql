CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_accounts_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_accounts_company_code
        UNIQUE (company_id, account_code)
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    entry_number VARCHAR(100) NOT NULL,
    entry_date TIMESTAMP NOT NULL,
    description VARCHAR(500),
    reference_number VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_journal_entries_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_journal_entries_company_number
        UNIQUE (company_id, entry_number)
);

CREATE TABLE journal_entry_lines (
    id UUID PRIMARY KEY,
    journal_entry_id UUID NOT NULL,
    account_id UUID NOT NULL,
    description VARCHAR(500),
    debit_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    credit_amount NUMERIC(14,2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_journal_entry_lines_entry
        FOREIGN KEY (journal_entry_id)
        REFERENCES journal_entries(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_journal_entry_lines_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_journal_entry_lines_debit
        CHECK (debit_amount >= 0),

    CONSTRAINT chk_journal_entry_lines_credit
        CHECK (credit_amount >= 0),

    CONSTRAINT chk_journal_entry_lines_one_side
        CHECK (
            (debit_amount > 0 AND credit_amount = 0)
            OR
            (credit_amount > 0 AND debit_amount = 0)
        )
);

CREATE INDEX idx_accounts_company_id
    ON accounts(company_id);

CREATE INDEX idx_accounts_type
    ON accounts(account_type);

CREATE INDEX idx_journal_entries_company_id
    ON journal_entries(company_id);

CREATE INDEX idx_journal_entries_entry_date
    ON journal_entries(entry_date);

CREATE INDEX idx_journal_entry_lines_entry_id
    ON journal_entry_lines(journal_entry_id);

CREATE INDEX idx_journal_entry_lines_account_id
    ON journal_entry_lines(account_id);