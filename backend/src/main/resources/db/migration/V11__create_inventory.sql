CREATE TABLE inventory (
    id UUID PRIMARY KEY,
    warehouse_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(12,3) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT uk_inventory_warehouse_product
        UNIQUE (warehouse_id, product_id),

    CONSTRAINT chk_inventory_quantity
        CHECK (quantity >= 0),

    CONSTRAINT chk_inventory_reserved_quantity
        CHECK (reserved_quantity >= 0),

    CONSTRAINT chk_inventory_reserved_not_greater
        CHECK (reserved_quantity <= quantity)
);

CREATE INDEX idx_inventory_warehouse_id
    ON inventory(warehouse_id);

CREATE INDEX idx_inventory_product_id
    ON inventory(product_id);