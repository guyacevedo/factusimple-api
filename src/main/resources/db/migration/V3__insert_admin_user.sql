-- Enable pgcrypto extension for crypt function if not already enabled
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Inserta el usuario administrador inicial si no existe
INSERT INTO users (
    id, email, password, first_name, last_name, phone,
    role_id, plan_id,
    products_count, customers_count, invoice_count,
    is_active, last_login,
    created_at, updated_at, created_by, updated_by
)
SELECT
    uuid_generate_v4(),
    'admin@apifactus.com',
    crypt('Admin1234', gen_salt('bf')),
    'Super',
    'Admin',
    NULL,
    r.id,
    p.id,
    0,
    0,
    0,
    TRUE,
    NULL,
    NOW(),
    NOW(),
    NULL,
    NULL
FROM roles r, plans p
WHERE r.name = 'Admin' AND p.name = 'FREE'
ON CONFLICT (email) DO NOTHING;