# Database Design Notes

## Main entities

- Department: stores company departments and cost centres.
- Employee: stores employees and links each employee to one department.
- Floor: stores office building and floor information.
- Zone: divides each floor into work areas with a noise level.
- Seat: stores individual bookable seats and equipment details.
- Booking: stores seat reservations made by employees.
- MaintenanceTicket: stores seat issues reported by employees.
- AuditLog: stores automatic booking activity logs.

## Relationships

- One department has many employees.
- One floor has many zones.
- One zone has many seats.
- One employee can create many bookings.
- One seat can have many bookings over time.
- One seat can have many maintenance tickets.
- One employee can report many maintenance tickets.

## Relational model

- departments(department_id PK, department_name, cost_centre)
- employees(employee_id PK, employee_no, full_name, email, role_title, work_mode, department_id FK)
- floors(floor_id PK, building_name, floor_number, floor_name)
- zones(zone_id PK, floor_id FK, zone_name, noise_level)
- seats(seat_id PK, seat_code, zone_id FK, seat_type, has_monitor, has_docking, status)
- bookings(booking_id PK, seat_id FK, employee_id FK, booking_date, start_time, end_time, purpose, status, created_at)
- maintenance_tickets(ticket_id PK, seat_id FK, reported_by FK, issue_title, issue_detail, priority, status, reported_at, resolved_at)
- audit_log(audit_id PK, entity_name, entity_id, action_name, action_time, details)

## Database features used

- Primary keys for entity identity.
- Foreign keys for referential integrity.
- Unique constraints on employee numbers, emails, seat codes and floor identities.
- Check constraint to ensure booking start time is before end time.
- Indexes for frequent query fields.
- Views for seat directory and booking overview.
- Triggers for audit logging and maintenance resolved timestamps.

Additional trigger rules: the database rejects overlapping active bookings for the same seat, writes audit records when bookings are created, marks seats as maintenance when a ticket is reported, and records resolved timestamps when tickets are closed.
