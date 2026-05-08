-- Inserta roles por defecto
INSERT INTO roles (id, name, description, created_at, updated_at, created_by, updated_by) 
VALUES (uuid_generate_v4(), 'Admin', 'Administrador del sistema', NOW(), NOW(), NULL, NULL) 
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description, created_at, updated_at, created_by, updated_by) 
VALUES (uuid_generate_v4(), 'Establishment', 'Usuario regular', NOW(), NOW(), NULL, NULL) 
ON CONFLICT (name) DO NOTHING;

-- Inserta el plan inicial obligatorio
INSERT INTO plans (id, name, max_products, max_customers, max_invoices, required_invited_code, description, is_active, created_at, updated_at, created_by, updated_by) 
VALUES (uuid_generate_v4(), 'FREE', 20, 10, 10, FALSE, 'Plan gratuito por defecto: hasta 10 facturas', TRUE, NOW(), NOW(), NULL, NULL) 
ON CONFLICT (name) DO NOTHING;