CREATE TABLE stock_transfers (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    source_warehouse_id UUID NOT NULL,
    destination_warehouse_id UUID NOT NULL,
    transfer_number VARCHAR(100) NOT NULL,
    transfer_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_stock_transfers_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_stock_transfers_source_warehouse
        FOREIGN KEY (source_warehouse_id)
        REFERENCES warehouses(id),

    CONSTRAINT fk_stock_transfers_destination_warehouse
        FOREIGN KEY (destination_warehouse_id)
        REFERENCES warehouses(id),

    CONSTRAINT uk_stock_transfers_company_number
        UNIQUE (company_id, transfer_number),

    CONSTRAINT chk_stock_transfers_different_warehouses
        CHECK (source_warehouse_id <> destination_warehouse_id)
);

CREATE TABLE stock_transfer_items (
    id UUID PRIMARY KEY,
    transfer_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,

    CONSTRAINT fk_stock_transfer_items_transfer
        FOREIGN KEY (transfer_id)
        REFERENCES stock_transfers(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_stock_transfer_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_stock_transfer_items_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_transfers_company_id
    ON stock_transfers(company_id);

CREATE INDEX idx_stock_transfers_source_warehouse_id
    ON stock_transfers(source_warehouse_id);

CREATE INDEX idx_stock_transfers_destination_warehouse_id
    ON stock_transfers(destination_warehouse_id);

CREATE INDEX idx_stock_transfer_items_transfer_id
    ON stock_transfer_items(transfer_id);

CREATE INDEX idx_stock_transfer_items_product_id
    ON stock_transfer_items(product_id);