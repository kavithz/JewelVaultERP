CREATE TABLE stock_adjustments (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    adjustment_number VARCHAR(100) NOT NULL,
    adjustment_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_stock_adjustments_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_stock_adjustments_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id),

    CONSTRAINT uk_stock_adjustments_company_number
        UNIQUE (company_id, adjustment_number)
);

CREATE TABLE stock_adjustment_items (
    id UUID PRIMARY KEY,
    adjustment_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    adjustment_type VARCHAR(30) NOT NULL,

    CONSTRAINT fk_stock_adjustment_items_adjustment
        FOREIGN KEY (adjustment_id)
        REFERENCES stock_adjustments(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_stock_adjustment_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_stock_adjustment_items_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_adjustments_company_id
    ON stock_adjustments(company_id);

CREATE INDEX idx_stock_adjustments_warehouse_id
    ON stock_adjustments(warehouse_id);

CREATE INDEX idx_stock_adjustment_items_adjustment_id
    ON stock_adjustment_items(adjustment_id);

CREATE INDEX idx_stock_adjustment_items_product_id
    ON stock_adjustment_items(product_id);