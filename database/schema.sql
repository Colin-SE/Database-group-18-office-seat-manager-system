DROP DATABASE IF EXISTS office_seat_manager;
CREATE DATABASE office_seat_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE office_seat_manager;

CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(80) NOT NULL UNIQUE,
    cost_centre VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_no VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role_title VARCHAR(80) NOT NULL,
    work_mode ENUM('ONSITE','HYBRID','REMOTE') NOT NULL DEFAULT 'HYBRID',
    department_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE floors (
    floor_id INT AUTO_INCREMENT PRIMARY KEY,
    building_name VARCHAR(80) NOT NULL,
    floor_number INT NOT NULL,
    floor_name VARCHAR(80) NOT NULL,
    UNIQUE(building_name, floor_number)
);

CREATE TABLE zones (
    zone_id INT AUTO_INCREMENT PRIMARY KEY,
    floor_id INT NOT NULL,
    zone_name VARCHAR(80) NOT NULL,
    noise_level ENUM('QUIET','NORMAL','COLLABORATIVE') NOT NULL,
    FOREIGN KEY (floor_id) REFERENCES floors(floor_id),
    UNIQUE(floor_id, zone_name)
);

CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    seat_code VARCHAR(20) NOT NULL UNIQUE,
    zone_id INT NOT NULL,
    seat_type ENUM('STANDARD','STANDING','ACCESSIBLE','MEETING_POD') NOT NULL,
    has_monitor BOOLEAN NOT NULL DEFAULT FALSE,
    has_docking BOOLEAN NOT NULL DEFAULT FALSE,
    status ENUM('AVAILABLE','RESERVED','MAINTENANCE','DISABLED') NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (zone_id) REFERENCES zones(zone_id)
);

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    employee_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    purpose VARCHAR(160) NOT NULL,
    status ENUM('ACTIVE','CANCELLED','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CHECK (start_time < end_time)
);

CREATE TABLE maintenance_tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    reported_by INT NOT NULL,
    issue_title VARCHAR(120) NOT NULL,
    issue_detail TEXT NOT NULL,
    priority ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('OPEN','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'OPEN',
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
    FOREIGN KEY (reported_by) REFERENCES employees(employee_id)
);

CREATE TABLE audit_log (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(40) NOT NULL,
    entity_id INT NOT NULL,
    action_name VARCHAR(40) NOT NULL,
    action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(255) NOT NULL
);

CREATE INDEX idx_bookings_date ON bookings(booking_date);
CREATE INDEX idx_employee_department ON employees(department_id);
CREATE INDEX idx_seat_zone ON seats(zone_id);

CREATE VIEW seat_directory AS
SELECT s.seat_id, s.seat_code, s.seat_type, s.has_monitor, s.has_docking, s.status,
       z.zone_name, z.noise_level, f.building_name, f.floor_number, f.floor_name
FROM seats s
JOIN zones z ON s.zone_id = z.zone_id
JOIN floors f ON z.floor_id = f.floor_id;

CREATE VIEW booking_overview AS
SELECT b.booking_id, b.booking_date, b.start_time, b.end_time, b.status, b.purpose,
       e.employee_no, e.full_name, e.email, d.department_name,
       sd.seat_code, sd.seat_type, sd.zone_name, sd.building_name, sd.floor_number
FROM bookings b
JOIN employees e ON b.employee_id = e.employee_id
JOIN departments d ON e.department_id = d.department_id
JOIN seat_directory sd ON b.seat_id = sd.seat_id;

DELIMITER //
CREATE TRIGGER trg_booking_no_overlap
BEFORE INSERT ON bookings
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM bookings
        WHERE seat_id = NEW.seat_id
          AND booking_date = NEW.booking_date
          AND status = 'ACTIVE'
          AND start_time < NEW.end_time
          AND end_time > NEW.start_time
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seat already has an active booking in this time range';
    END IF;
END//

CREATE TRIGGER trg_booking_insert_audit
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(entity_name, entity_id, action_name, details)
    VALUES ('booking', NEW.booking_id, 'CREATE', CONCAT('Seat ', NEW.seat_id, ' booked by employee ', NEW.employee_id));
END//

CREATE TRIGGER trg_ticket_mark_seat_maintenance
AFTER INSERT ON maintenance_tickets
FOR EACH ROW
BEGIN
    UPDATE seats SET status = 'MAINTENANCE' WHERE seat_id = NEW.seat_id;
END//

CREATE TRIGGER trg_ticket_resolve_time
BEFORE UPDATE ON maintenance_tickets
FOR EACH ROW
BEGIN
    IF NEW.status = 'RESOLVED' AND OLD.status <> 'RESOLVED' THEN
        SET NEW.resolved_at = CURRENT_TIMESTAMP;
    END IF;
END//
DELIMITER ;

