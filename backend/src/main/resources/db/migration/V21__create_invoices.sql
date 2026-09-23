CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    invoice_number VARCHAR(100) NOT NULL,
    invoice_date TIMESTAMP NOT NULL,
    invoice_type VARCHAR(30) NOT NULL,
    customer_id UUID,
    supplier_id UUID,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_invoices_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_invoices_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_invoices_supplier
        FOREIGN KEY (supplier_id)
        REFERENCES suppliers(id),

    CONSTRAINT uk_invoices_company_number
        UNIQUE (company_id, invoice_number),

    CONSTRAINT chk_invoices_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT chk_invoices_tax
        CHECK (tax_amount >= 0),

    CONSTRAINT chk_invoices_discount
        CHECK (discount_amount >= 0),

    CONSTRAINT chk_invoices_total
        CHECK (total_amount >= 0)
);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(14,2) NOT NULL,
    tax_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_price NUMERIC(14,2) NOT NULL,

    CONSTRAINT fk_invoice_items_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_invoice_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_invoice_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_invoice_items_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_invoice_items_tax
        CHECK (tax_amount >= 0),

    CONSTRAINT chk_invoice_items_discount
        CHECK (discount_amount >= 0),

    CONSTRAINT chk_invoice_items_total
        CHECK (total_price >= 0)
);

CREATE INDEX idx_invoices_company_id
    ON invoices(company_id);

CREATE INDEX idx_invoices_customer_id
    ON invoices(customer_id);

CREATE INDEX idx_invoices_supplier_id
    ON invoices(supplier_id);

CREATE INDEX idx_invoices_invoice_date
    ON invoices(invoice_date);

CREATE INDEX idx_invoice_items_invoice_id
    ON invoice_items(invoice_id);

CREATE INDEX idx_invoice_items_product_id
    ON invoice_items(product_id);