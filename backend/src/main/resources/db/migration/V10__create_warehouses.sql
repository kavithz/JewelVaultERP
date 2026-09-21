CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_warehouses_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(id),

    CONSTRAINT uk_warehouses_branch_code
        UNIQUE (branch_id, code)
);

CREATE INDEX idx_warehouses_branch_id
    ON warehouses(branch_id);

CREATE INDEX idx_warehouses_active
    ON warehouses(active);