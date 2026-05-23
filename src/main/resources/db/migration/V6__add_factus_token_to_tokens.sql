-- Add factus_token column to tokens table to store Factus credentials separately from internal JWT
ALTER TABLE tokens ADD COLUMN factus_token TEXT;
