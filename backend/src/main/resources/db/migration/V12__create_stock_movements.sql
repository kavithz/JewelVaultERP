CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    inventory_id UUID NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    reference_number VARCHAR(100),
    notes VARCHAR(500),
    movement_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_stock_movements_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES inventory(id),

    CONSTRAINT chk_stock_movements_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_movements_inventory_id
    ON stock_movements(inventory_id);

CREATE INDEX idx_stock_movements_type
    ON stock_movements(movement_type);

CREATE INDEX idx_stock_movements_date
    ON stock_movements(movement_date);