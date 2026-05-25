# Office Seat Manager

基于javafx ，mysql，maven构建的办公室座位管理系统

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

```程序运行入口
mvn javafx:run
```

## Main functions主要功能


- 查询今日可用座位
- 创建座位预订
- 查看员工信息
- 提交维护工单
- 查看楼层占用率、部门预订统计和审计日志
- 查看所有座位信息

