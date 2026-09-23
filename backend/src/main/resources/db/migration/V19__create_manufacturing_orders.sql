CREATE TABLE manufacturing_orders (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    order_number VARCHAR(100) NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_manufacturing_orders_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_manufacturing_orders_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id),

    CONSTRAINT uk_manufacturing_orders_company_number
        UNIQUE (company_id, order_number)
);

CREATE TABLE manufacturing_order_items (
    id UUID PRIMARY KEY,
    manufacturing_order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    item_type VARCHAR(30) NOT NULL,

    CONSTRAINT fk_manufacturing_order_items_order
        FOREIGN KEY (manufacturing_order_id)
        REFERENCES manufacturing_orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_manufacturing_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_manufacturing_order_items_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_manufacturing_orders_company_id
    ON manufacturing_orders(company_id);

CREATE INDEX idx_manufacturing_orders_warehouse_id
    ON manufacturing_orders(warehouse_id);

CREATE INDEX idx_manufacturing_order_items_order_id
    ON manufacturing_order_items(manufacturing_order_id);

CREATE INDEX idx_manufacturing_order_items_product_id
    ON manufacturing_order_items(product_id);