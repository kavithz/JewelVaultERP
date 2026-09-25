CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    user_id UUID,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    reference_type VARCHAR(100),
    reference_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,

    CONSTRAINT fk_notifications_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT chk_notifications_type
        CHECK (
            notification_type IN (
                'LOW_STOCK',
                'OUT_OF_STOCK',
                'PAYMENT_DUE',
                'PAYMENT_OVERDUE',
                'RECEIVABLE_DUE',
                'PAYABLE_DUE',
                'EXPENSE_DUE',
                'PURCHASE_CREATED',
                'SALE_CREATED',
                'INVOICE_CREATED',
                'APPROVAL_REQUIRED',
                'PAYROLL_PROCESSED',
                'SYSTEM',
                'OTHER'
            )
        )
);

CREATE INDEX idx_notifications_company_id
    ON notifications(company_id);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_company_user_read
    ON notifications(company_id, user_id, is_read);

CREATE INDEX idx_notifications_company_read
    ON notifications(company_id, is_read);

CREATE INDEX idx_notifications_company_type
    ON notifications(company_id, notification_type);

CREATE INDEX idx_notifications_created_at
    ON notifications(created_at DESC);
