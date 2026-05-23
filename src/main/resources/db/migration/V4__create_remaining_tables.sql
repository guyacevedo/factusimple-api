-- Create remaining tables for factusimple-api
-- Tables for auth, core entities, and invoicing

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    token_type VARCHAR(20) NOT NULL CHECK (token_type IN ('ACCESS', 'REFRESH')),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_token_user_id ON tokens(user_id);
CREATE INDEX idx_token_expires_at ON tokens(expires_at);

CREATE TABLE establishments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    municipality_code VARCHAR(5),
    nit VARCHAR(20) NOT NULL,
    dv VARCHAR(2),
    legal_org_code VARCHAR(10),
    tribute_code VARCHAR(4),
    fiscal_responsibility VARCHAR(50),
    numbering_range_id INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_establishment_user_id ON establishments(user_id);
CREATE INDEX idx_establishment_nit ON establishments(nit);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock NUMERIC(12, 2),
    unit_measure_code VARCHAR(10),
    standard_code VARCHAR(10),
    tax_code VARCHAR(4),
    tax_rate NUMERIC(5, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    UNIQUE (establishment_id, sku)
);
CREATE INDEX idx_product_establishment_id ON products(establishment_id);
CREATE INDEX idx_product_sku ON products(sku);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    id_type_code VARCHAR(4) NOT NULL,
    identification VARCHAR(30) NOT NULL,
    dv VARCHAR(2),
    legal_org_code VARCHAR(10),
    company VARCHAR(255),
    names VARCHAR(255),
    trade_name VARCHAR(255),
    address VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(20),
    municipality_code VARCHAR(10),
    tribute_code VARCHAR(4),
    fiscal_responsibility VARCHAR(50),
    credit_limit NUMERIC(12, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    UNIQUE (establishment_id, identification)
);
CREATE INDEX idx_customer_establishment_id ON customers(establishment_id);
CREATE INDEX idx_customer_identification ON customers(identification);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    reference_code VARCHAR(100) NOT NULL,
    document_type VARCHAR(4),
    operation_type VARCHAR(20),
    send_email BOOLEAN NOT NULL DEFAULT FALSE,
    observation TEXT,
    cash_rounding NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL,
    subtotal NUMERIC(16, 2) NOT NULL DEFAULT 0.00,
    total_taxes NUMERIC(16, 2) NOT NULL DEFAULT 0.00,
    total_discounts NUMERIC(16, 2) NOT NULL DEFAULT 0.00,
    total NUMERIC(16, 2) NOT NULL DEFAULT 0.00,
    cufe VARCHAR(100),
    xml_url TEXT,
    factus_number VARCHAR(50),
    factus_error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    UNIQUE (establishment_id, reference_code)
);
CREATE INDEX idx_invoice_establishment_id ON invoices(establishment_id);
CREATE INDEX idx_invoice_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoice_reference_code ON invoices(reference_code);
CREATE INDEX idx_invoice_status ON invoices(status);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    code_reference VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    quantity NUMERIC(12, 2) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    discount_rate NUMERIC(5, 2),
    unit_measure_code VARCHAR(10),
    standard_code VARCHAR(10),
    note TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_invoice_item_invoice_id ON invoice_items(invoice_id);
CREATE INDEX idx_invoice_item_product_id ON invoice_items(product_id);

CREATE TABLE invoice_item_taxes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    item_id UUID NOT NULL REFERENCES invoice_items(id) ON DELETE CASCADE,
    tax_code VARCHAR(4) NOT NULL,
    tax_rate NUMERIC(5, 2) NOT NULL,
    tax_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_invoice_item_tax_item_id ON invoice_item_taxes(item_id);

CREATE TABLE invoice_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    payment_method_code VARCHAR(4) NOT NULL,
    payment_amount NUMERIC(12, 2) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_invoice_payment_invoice_id ON invoice_payments(invoice_id);

CREATE TABLE invoice_prepayments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    prepayment_id VARCHAR(100),
    prepayment_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_invoice_prepayment_invoice_id ON invoice_prepayments(invoice_id);

CREATE TABLE allowance_charges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID REFERENCES invoices(id) ON DELETE CASCADE,
    charge_indicator BOOLEAN NOT NULL,
    reason_code VARCHAR(4) NOT NULL,
    reason_description TEXT,
    amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_allowance_charge_invoice_id ON allowance_charges(invoice_id);

CREATE TABLE credit_notes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    invoice_id UUID REFERENCES invoices(id) ON DELETE SET NULL,
    reference_code VARCHAR(100) NOT NULL,
    correction_concept_code VARCHAR(100) NOT NULL,
    customization_id VARCHAR(2) DEFAULT '20',
    observation TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    factus_number VARCHAR(50),
    cufe VARCHAR(100),
    xml_url TEXT,
    factus_error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    UNIQUE (establishment_id, reference_code)
);
CREATE INDEX idx_credit_note_invoice ON credit_notes(invoice_id);
CREATE INDEX idx_credit_note_establishment ON credit_notes(establishment_id);
CREATE INDEX idx_credit_note_status ON credit_notes(status);

CREATE TABLE credit_note_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    credit_note_id UUID NOT NULL REFERENCES credit_notes(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    code_reference VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    quantity NUMERIC(12, 2) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    discount_rate NUMERIC(5, 2),
    unit_measure_code VARCHAR(10),
    standard_code VARCHAR(10),
    note TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_credit_note_item_credit_note_id ON credit_note_items(credit_note_id);
CREATE INDEX idx_credit_note_item_product_id ON credit_note_items(product_id);

CREATE TABLE credit_note_item_taxes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    item_id UUID NOT NULL REFERENCES credit_note_items(id) ON DELETE CASCADE,
    tax_code VARCHAR(4) NOT NULL,
    tax_rate NUMERIC(5, 2) NOT NULL,
    tax_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_credit_note_item_tax_item_id ON credit_note_item_taxes(item_id);

CREATE TABLE credit_note_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    credit_note_id UUID NOT NULL REFERENCES credit_notes(id) ON DELETE CASCADE,
    payment_method_code VARCHAR(4) NOT NULL,
    payment_amount NUMERIC(12, 2) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_credit_note_payment_credit_note_id ON credit_note_payments(credit_note_id);

CREATE TABLE credit_note_allowance_charges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    credit_note_id UUID NOT NULL REFERENCES credit_notes(id) ON DELETE CASCADE,
    charge_indicator BOOLEAN NOT NULL,
    reason_code VARCHAR(4) NOT NULL,
    reason_description TEXT,
    amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_credit_note_allowance_charge_credit_note_id ON credit_note_allowance_charges(credit_note_id);
