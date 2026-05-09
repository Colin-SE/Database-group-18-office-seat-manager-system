# Office Seat Manager

Office Seat Manager is a JavaFX + JDBC + MySQL information system for managing office seats, employees, bookings and maintenance tickets.

## Technology

- Java 21
- JavaFX 21
- JDBC
- MySQL
- Maven
- No ORM framework is used

## Database setup

1. Open MySQL.
2. Run `database/schema.sql`.
3. Run `database/seed.sql`.
4. Edit `config/database.properties` and replace `your_password` with your MySQL password.

Example:

```properties
db.url=jdbc:mysql://localhost:3306/office_seat_manager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=123456
```

## Run the application

From this folder:

```powershell
mvn javafx:run
```

## Main functions

- View all seats with building, floor, zone, type and equipment.
- Search seats by seat code, zone or building.
- Show available seats for today.
- Create a seat booking with overlap checking.
- View all booking records.
- View and search employees.
- Create maintenance tickets.
- View occupancy and department booking reports.
- Review audit logs created by database triggers.

## Assignment fit

The project is a Java-based database information system. It uses a relational MySQL database with primary keys, foreign keys, unique constraints, check constraints, indexes, views and triggers. The JavaFX interface provides CRUD-style operations and reporting without using ORM or a complicated MVC framework.
