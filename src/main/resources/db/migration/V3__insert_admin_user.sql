-- Default ADMIN user (password must be changed on first login)
INSERT INTO users (
    id, email, password,
    first_name, last_name,
    role, plan_id,
    product_count, customer_count, invoice_count,
    is_active, last_login,
    created_at, updated_at, created_by, updated_by
)
VALUES (
    '00000000-0000-0000-0000-000000000001'::uuid,
    'admin@factusimple.com',
    '$2a$12$D4fU7v1YOJOQJETzpyO/xuyRNrA3V/3UBhXuMR7Kp07LbQslmGJou',
    'Admin', 'FactuSimple',
    'ADMIN',
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    0, 0, 0,
    FALSE, NULL,
    NOW(), NOW(), NULL, NULL
)
ON CONFLICT (email) DO NOTHING;
