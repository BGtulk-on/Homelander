CREATE TYPE equipment_status AS ENUM (
    'Available', 
    'Checked_Out', 
    'Under_Repair', 
    'Retired'
);

CREATE TYPE request_status_type AS ENUM (
    'PENDING', 
    'APPROVED', 
    'REJECTED', 
    'RETURNED'
);

CREATE TYPE equipment_condition AS ENUM (
    'POOR', 
    'GOOD', 
    'VERY_GOOD', 
    'EXCELLENT'
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    approved BOOLEAN DEFAULT FALSE
);

CREATE TABLE contacts (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE equipment_types (
    id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE locations (
    id SERIAL PRIMARY KEY,
    room_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type_id INT REFERENCES equipment_types(id) ON DELETE SET NULL,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    status equipment_status DEFAULT 'Available',
    current_condition equipment_condition,
    location_id INT REFERENCES locations(id) ON DELETE SET NULL,
    photo_url VARCHAR(255),
    is_assigned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE requests (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    equipment_id INT NOT NULL REFERENCES equipment(id) ON DELETE CASCADE,
    request_status request_status_type DEFAULT 'PENDING',
    requested_start_date TIMESTAMPTZ NOT NULL,
    requested_end_date TIMESTAMPTZ NOT NULL,
    approved_by_admin_id INT REFERENCES users(id) ON DELETE SET NULL,
    actual_return_date TIMESTAMPTZ,
    return_condition equipment_condition,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT check_dates CHECK (requested_end_date > requested_start_date)
);

CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    equipment_id INT NOT NULL REFERENCES equipment(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_requests_status ON requests(request_status);
CREATE INDEX idx_equipment_serial ON equipment(serial_number);

-- ======================
-- Users (first user is admin and the last user is a superuser)
-- ======================
INSERT INTO users (first_name, last_name, email, password_hash, role)
VALUES 
('Ivan', 'Petrov', 'ivan.petrov@example.com', 'HASHED_PASSWORD_1', 'ADMIN'),   -- admin
('Maria', 'Georgieva', 'maria.georgieva@example.com', 'HASHED_PASSWORD_2', 'USER'),
('Georgi', 'Ivanov', 'georgi.ivanov@example.com', 'HASHED_PASSWORD_3', 'SUPERUSER');

-- ======================
-- Contacts
-- ======================
INSERT INTO contacts (user_id, phone, address)
VALUES
(1, '+359888111222', 'Sofia, Bulgaria'),
(2, '+359888333444', 'Plovdiv, Bulgaria'),
(3, '+359888555666', 'Varna, Bulgaria');

-- ======================
-- Equipment Types
-- ======================
INSERT INTO equipment_types (type_name)
VALUES
('Laptop'),
('Projector'),
('Camera'),
('Tablet');

-- ======================
-- Locations (Rooms)
-- ======================
INSERT INTO locations (room_name)
VALUES
('Room 101'),
('Room 102'),
('Lab A'),
('Lab B');

-- ======================
-- Equipment
-- ======================
INSERT INTO equipment (name, type_id, serial_number, status, current_condition, location_id, photo_url)
VALUES
('Dell XPS 13', 1, 'SN1001', 'Available', 'EXCELLENT', 1, 'https://example.com/dellxps13.png'),
('Canon EOS 5D', 3, 'SN2001', 'Checked_Out', 'VERY_GOOD', 3, 'https://example.com/canon5d.png'),
('Epson Projector', 2, 'SN3001', 'Under_Repair', 'GOOD', 2, 'https://example.com/epsonproj.png'),
('iPad Pro', 4, 'SN4001', 'Available', 'EXCELLENT', 4, 'https://example.com/ipadpro.png');

-- ======================
-- Requests
-- ======================
INSERT INTO requests (user_id, equipment_id, request_status, requested_start_date, requested_end_date, approved_by_admin_id, actual_return_date, return_condition)
VALUES
(2, 1, 'APPROVED', '2026-03-10 08:00:00+02', '2026-03-15 18:00:00+02', 1, '2026-03-15 17:30:00+02', 'EXCELLENT'),
(3, 2, 'PENDING', '2026-03-12 09:00:00+02', '2026-03-14 17:00:00+02', NULL, NULL, NULL);

-- ======================
-- Documents
-- ======================
INSERT INTO documents (equipment_id, file_name, file_url)
VALUES
(1, 'Dell XPS Manual.pdf', 'https://example.com/manuals/dellxps13.pdf'),
(2, 'Canon EOS Guide.pdf', 'https://example.com/manuals/canon5d.pdf'),
(3, 'Epson Projector Specs.pdf', 'https://example.com/manuals/epsonproj.pdf');