DO $$
BEGIN
    -- Drop tables
    DROP TABLE IF EXISTS documents CASCADE;
    DROP TABLE IF EXISTS requests CASCADE;
    DROP TABLE IF EXISTS equipment CASCADE;
    DROP TABLE IF EXISTS locations CASCADE;
    DROP TABLE IF EXISTS equipment_types CASCADE;
    DROP TABLE IF EXISTS contacts CASCADE;
    DROP TABLE IF EXISTS users CASCADE;

    -- Drop types
    DROP TYPE IF EXISTS equipment_status CASCADE;
    DROP TYPE IF EXISTS request_status_type CASCADE;
    DROP TYPE IF EXISTS equipment_condition CASCADE;
END $$;
