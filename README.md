# Office Seat Manager

基于javafx ，mysql，maven构建的办公室作为管理系统

## Technology

- Java 21
- JavaFX 21
- JDBC
- MySQL
- Maven

## Database setup

1. 打开 MySQL.
2. 运行`database/schema.sql`.
3. 运行`database/seed.sql`.
4. 编辑`config/database.properties` 并且将 `your_password` 部分替换成真实的mysql密码

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


