CREATE TABLE users (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_users_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT uk_users_username
        UNIQUE (username),

    CONSTRAINT uk_users_email
        UNIQUE (email)
);

CREATE INDEX idx_users_company_id ON users(company_id);
