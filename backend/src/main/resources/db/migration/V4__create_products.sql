CREATE TABLE products (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    jewellery_type VARCHAR(100) NOT NULL,
    metal_type VARCHAR(100) NOT NULL,
    purity VARCHAR(50) NOT NULL,
    gross_weight NUMERIC(12,3) NOT NULL,
    net_weight NUMERIC(12,3) NOT NULL,
    making_charge NUMERIC(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_products_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id)
);

CREATE INDEX idx_products_company_id
    ON products(company_id);

CREATE INDEX idx_products_sku
    ON products(sku);

CREATE INDEX idx_products_active
    ON products(active);