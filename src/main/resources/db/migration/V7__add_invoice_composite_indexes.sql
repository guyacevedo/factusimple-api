-- Add composite indexes for invoice queries (Sprint 3 - Performance optimization P-5)
CREATE INDEX idx_invoice_est_status ON invoices(establishment_id, status);
CREATE INDEX idx_invoice_est_customer ON invoices(establishment_id, customer_id);
CREATE INDEX idx_invoice_est_created ON invoices(establishment_id, created_at);
