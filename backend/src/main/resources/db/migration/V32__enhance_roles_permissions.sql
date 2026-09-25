ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS company_id UUID;

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

UPDATE roles
SET company_id = (
    SELECT id
    FROM companies
    ORDER BY created_at
    LIMIT 1
)
WHERE company_id IS NULL;

ALTER TABLE roles
    ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE roles
    DROP CONSTRAINT IF EXISTS roles_name_key;

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_roles_company_name
    ON roles(company_id, name);

CREATE INDEX IF NOT EXISTS idx_roles_company_id
    ON roles(company_id);

CREATE INDEX IF NOT EXISTS idx_roles_active
    ON roles(active);
