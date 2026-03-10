CREATE TYPE equipment_status AS ENUM (
    'Available', 
    'Checked Out', 
    'Under Repair', 
    'Retired'
);

CREATE TYPE request_status_type AS ENUM (
    'PENDING', 
    'APPROVED', 
    'REJECTED', 
    'RETURNED'
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE admins (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    status equipment_status DEFAULT 'Available',
    current_condition TEXT,
    location VARCHAR(100),
    photo_url VARCHAR(255),
    is_sensitive BOOLEAN DEFAULT FALSE,
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
    
    approved_by_admin_id INT REFERENCES admins(id) ON DELETE SET NULL,
    
    actual_return_date TIMESTAMPTZ,
    return_condition_log TEXT,
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