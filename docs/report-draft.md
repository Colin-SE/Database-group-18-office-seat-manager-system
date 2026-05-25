# Office Seat Manager Report Draft

## 1. System Description

Office Seat Manager is an information system designed for organisations that use hybrid working or shared office desks. The system allows staff and administrators to view available seats, create seat bookings, manage employees, report maintenance issues and review usage statistics. The system assumes that each seat belongs to a zone, each zone belongs to a floor, and each employee belongs to a department.

## 2. System Design

The system uses a JavaFX desktop user interface connected to a MySQL database through JDBC. The design avoids ORM frameworks and keeps SQL visible in the Java code so that database operations can be clearly demonstrated. The database contains departments, employees, floors, zones, seats, bookings, maintenance tickets and audit logs.

## 3. Database Design

The ER model includes one-to-many relationships between departments and employees, floors and zones, zones and seats, employees and bookings, seats and bookings, and seats and maintenance tickets. The relational schema is implemented in `database/schema.sql`. The database uses primary keys, foreign keys, unique constraints, a check constraint, indexes, views and triggers.

## 4. Implementation

The JavaFX application provides five main tabs: Seats, Bookings, Employees, Maintenance and Reports. The Seats tab displays searchable seat information. The Bookings tab allows a user to create bookings while checking for time overlaps. The Employees tab lists and searches employee records. The Maintenance tab creates and displays maintenance tickets. The Reports tab shows occupancy, department booking counts and audit logs.

## 5. Interface

The interface is built with JavaFX controls such as TabPane, TableView, ComboBox, DatePicker and TextField. TableView is used to display database query results. Forms are used for booking and maintenance ticket creation.

## 6. Testing

Testing should include database connection testing, loading all seed data, creating a new booking, attempting an overlapping booking, creating a maintenance ticket, and checking whether audit log records are generated after booking creation.

## 7. AI Assistance Statement

AI assistance was used to help draft code, database design notes and report text. The final system should be reviewed and understood by the group before submission and interview.

## 8. Team Member Contribution

Replace this section with your real group members and their work. Example: database design, JavaFX interface, JDBC implementation, testing, report writing and video preparation.

## 9. Self-Assessment Records

Attach the week 10 and week 12 self-assessment and planning records as appendices, as required by the assignment brief.
