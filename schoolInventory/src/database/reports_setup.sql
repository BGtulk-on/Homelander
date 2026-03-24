-- Reports Table Setup for Equipment Management System
-- Run this in your PostgreSQL database (pgAdmin or psql)

-- Create the reports table
CREATE TABLE IF NOT EXISTS reports (
    id SERIAL PRIMARY KEY,
    equipment_id INT NOT NULL REFERENCES equipment(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    request_id INT NOT NULL REFERENCES requests(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_reports_equipment ON reports(equipment_id);
CREATE INDEX IF NOT EXISTS idx_reports_user ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_request ON reports(request_id);

-- Sample data setup (only if you have existing requests)
-- First, let's update some existing requests to be RETURNED status
DO $$
BEGIN
    -- Check if we have any requests and update some to RETURNED status
    IF EXISTS (SELECT 1 FROM requests WHERE request_status = 'APPROVED' LIMIT 1) THEN
        UPDATE requests 
        SET request_status = 'RETURNED', 
            actual_return_date = NOW(),
            return_condition = 'GOOD'
        WHERE request_status = 'APPROVED' 
        LIMIT 3;
        
        RAISE NOTICE 'Updated 3 requests to RETURNED status';
    END IF;
END $$;

-- Create report entries for returned requests
INSERT INTO reports (equipment_id, user_id, request_id)
SELECT r.equipment_id, r.user_id, r.id
FROM requests r
WHERE r.request_status = 'RETURNED'
AND NOT EXISTS (
    SELECT 1 FROM reports rep WHERE rep.request_id = r.id
);

-- Add sample returned requests if table is empty
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM requests WHERE request_status = 'RETURNED' LIMIT 1) THEN
        -- Insert sample users if they don't exist
        INSERT INTO users (first_name, last_name, email, password_hash, isadmin)
        VALUES 
        ('Ivan', 'Petrov', 'ivan.petrov@example.com', 'hashed_password_1', TRUE),
        ('Maria', 'Georgieva', 'maria.georgieva@example.com', 'hashed_password_2', FALSE),
        ('Georgi', 'Ivanov', 'georgi.ivanov@example.com', 'hashed_password_3', FALSE)
        ON CONFLICT (email) DO NOTHING;
        
        -- Insert sample equipment if it doesn't exist
        INSERT INTO equipment (name, type_id, serial_number, status, current_condition)
        VALUES 
        ('Dell XPS 13', 1, 'SN1001', 'Available', 'EXCELLENT'),
        ('Canon EOS 5D', 2, 'SN2001', 'Available', 'VERY_GOOD'),
        ('Epson Projector', 3, 'SN3001', 'Under_Repair', 'GOOD')
        ON CONFLICT (serial_number) DO NOTHING;
        
        -- Insert sample returned requests
        INSERT INTO requests (user_id, equipment_id, request_status, requested_start_date, requested_end_date, actual_return_date, return_condition)
        VALUES 
        (2, 1, 'RETURNED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 'EXCELLENT'),
        (3, 2, 'RETURNED', NOW() - INTERVAL '8 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'GOOD'),
        (2, 3, 'RETURNED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', 'VERY_GOOD');
        
        -- Create report entries
        INSERT INTO reports (equipment_id, user_id, request_id)
        SELECT equipment_id, user_id, id FROM requests WHERE request_status = 'RETURNED';
        
        RAISE NOTICE 'Created sample data for testing';
    END IF;
END $$;

-- Verify the setup
SELECT 
    'Reports Table' as table_name,
    COUNT(*) as record_count
FROM reports
UNION ALL
SELECT 
    'Returned Requests' as table_name,
    COUNT(*) as record_count
FROM requests 
WHERE request_status = 'RETURNED'
UNION ALL
SELECT 
    'Total Requests' as table_name,
    COUNT(*) as record_count
FROM requests;

-- Show sample data
SELECT 
    r.id as request_id,
    u.first_name || ' ' || u.last_name as user_name,
    u.email,
    e.name as equipment_name,
    e.serial_number,
    r.request_status,
    r.requested_start_date,
    r.requested_end_date,
    r.actual_return_date,
    r.return_condition
FROM requests r
JOIN users u ON r.user_id = u.id
JOIN equipment e ON r.equipment_id = e.id
WHERE r.request_status = 'RETURNED'
ORDER BY r.actual_return_date DESC;
