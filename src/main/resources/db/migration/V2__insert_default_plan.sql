-- Insert default FREE plan
INSERT INTO plans (id, name, max_products, max_customers, max_invoices, required_invited_code, description, is_active, created_at, updated_at, created_by, updated_by)
VALUES ('550e8400-e29b-41d4-a716-446655440000'::uuid, 'FREE', 20, 10, 10, FALSE, 'Plan gratuito por defecto: hasta 10 facturas', TRUE, NOW(), NOW(), NULL, NULL)
ON CONFLICT (name) DO NOTHING;
