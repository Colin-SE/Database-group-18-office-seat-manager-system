package com.assignment.seating;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.Properties;

/**
 * 办公室座位管理系统主程序。
 * 本系统使用 JavaFX 构建界面，使用 JDBC 直接连接 MySQL 数据库。
 * 项目没有使用 ORM，便于展示 SQL、关系模型和数据库约束。
 */
public class OfficeSeatManagerApp extends Application {
    private final Database database = new Database();
    private final TableView<ObservableList<String>> table = new TableView<>();
    private final Label status = new Label("Ready");
    private final ComboBox<Item> employeeBox = new ComboBox<>();
    private final ComboBox<Item> seatBox = new ComboBox<>();
    private final DatePicker bookingDate = new DatePicker(LocalDate.now());
    private final TextField startTime = new TextField("09:00");
    private final TextField endTime = new TextField("17:00");
    private final TextField purpose = new TextField("Desk booking");

    @Override
    public void start(Stage stage) {
        stage.setTitle("Office Seat Manager");
        BorderPane root = new BorderPane();
        root.setTop(header());
        root.setCenter(mainContent());
        root.setBottom(footer());
        Scene scene = new Scene(root, 1180, 760);
        var stylesheet = getClass().getResource("/style.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
        testConnection();
        loadSeats();
    }


    /** 主内容区：上方是功能标签页，下方是通用结果表格。 */
    private Node mainContent() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(0, 16, 16, 16));


        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);
        box.getChildren().addAll(tabs(), table);
        return box;
    }
    /** 顶部标题区域。 */
    private Node header() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(18, 22, 12, 22));
        Label title = new Label("Office Seat Manager");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label subtitle = new Label("JavaFX + JDBC + MySQL database information system");
        subtitle.setStyle("-fx-text-fill: #586070;");
        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private Node footer() {
        HBox box = new HBox(status);
        box.setPadding(new Insets(8, 16, 10, 16));
        box.setStyle("-fx-background-color: #f3f5f8; -fx-border-color: #d9dee8; -fx-border-width: 1 0 0 0;");
        return box;
    }

    /** 创建系统的五个功能页：座位、预订、员工、维护和报表。 */
    private TabPane tabs() {
        TabPane pane = new TabPane();
        pane.getTabs().add(tab("Seats", seatsView()));
        pane.getTabs().add(tab("Bookings", bookingsView()));
        pane.getTabs().add(tab("Employees", employeesView()));
        pane.getTabs().add(tab("Maintenance", maintenanceView()));
        pane.getTabs().add(tab("Reports", reportsView()));
        pane.getTabs().forEach(t -> t.setClosable(false));
        return pane;
    }

    private Tab tab(String title, Node content) {
        return new Tab(title, content);
    }

    /** 座位管理页：查看全部座位、搜索座位、查看今日可用座位。 */
    private Node seatsView() {
        VBox box = page();
        HBox actions = actions();
        Button refresh = button("Refresh seats");
        Button available = button("Available today");
        TextField keyword = new TextField();
        keyword.setPromptText("Search seat, zone, building");
        Button search = button("Search");
        refresh.setOnAction(e -> loadSeats());
        available.setOnAction(e -> loadAvailableSeats());
        search.setOnAction(e -> runQuery("Seats", "SELECT seat_code, seat_type, status, zone_name, noise_level, building_name, floor_number FROM seat_directory WHERE seat_code LIKE ? OR zone_name LIKE ? OR building_name LIKE ? ORDER BY seat_code", like(keyword.getText())));
        actions.getChildren().addAll(refresh, available, keyword, search);
        box.getChildren().add(actions);
        return box;
    }

    /** 预订管理页：选择员工、座位和时间段后创建预订。 */
    private Node bookingsView() {
        VBox box = page();
        GridPane form = formGrid();
        refreshCombos();
        Button reloadCombos = button("Reload choices");
        Button create = button("Create booking");
        Button show = button("Show bookings");
        reloadCombos.setOnAction(e -> refreshCombos());
        create.setOnAction(e -> createBooking());
        show.setOnAction(e -> loadBookings());
        form.addRow(0, new Label("Employee"), employeeBox, new Label("Seat"), seatBox, reloadCombos);
        form.addRow(1, new Label("Date"), bookingDate, new Label("Start"), startTime, new Label("End"), endTime);
        form.addRow(2, new Label("Purpose"), purpose, create, show);
        box.getChildren().add(form);
        return box;
    }

    /** 员工页：显示员工所属部门、岗位和工作模式。 */
    private Node employeesView() {
        VBox box = page();
        HBox actions = actions();
        Button all = button("All employees");
        TextField name = new TextField();
        name.setPromptText("Name or email");
        Button search = button("Search");
        all.setOnAction(e -> loadEmployees());
        search.setOnAction(e -> runQuery("Employees", "SELECT employee_no, full_name, email, role_title, work_mode, department_name FROM employees e JOIN departments d ON e.department_id=d.department_id WHERE full_name LIKE ? OR email LIKE ? ORDER BY full_name", like(name.getText())));
        actions.getChildren().addAll(all, name, search);
        box.getChildren().add(actions);
        return box;
    }

    /** 维护页：员工可以为有问题的座位提交维护工单。 */
    private Node maintenanceView() {
        VBox box = page();
        GridPane form = formGrid();
        ComboBox<Item> ticketSeat = new ComboBox<>();
        ComboBox<Item> reporter = new ComboBox<>();
        TextField title = new TextField();
        title.setPromptText("Issue title");
        TextField detail = new TextField();
        detail.setPromptText("Issue detail");
        ComboBox<String> priority = new ComboBox<>(FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH"));
        priority.setValue("MEDIUM");
        Button reload = button("Reload choices");
        Button report = button("Report issue");
        Button show = button("Show tickets");
        Runnable fill = () -> {
            ticketSeat.setItems(items("SELECT seat_id, seat_code FROM seats ORDER BY seat_code"));
            reporter.setItems(items("SELECT employee_id, full_name FROM employees WHERE is_active=TRUE ORDER BY full_name"));
        };
        fill.run();
        reload.setOnAction(e -> fill.run());
        report.setOnAction(e -> createTicket(ticketSeat.getValue(), reporter.getValue(), title.getText(), detail.getText(), priority.getValue()));
        show.setOnAction(e -> loadTickets());
        form.addRow(0, new Label("Seat"), ticketSeat, new Label("Reporter"), reporter, reload);
        form.addRow(1, new Label("Priority"), priority, new Label("Title"), title);
        form.addRow(2, new Label("Detail"), detail, report, show);
        box.getChildren().add(form);
        return box;
    }

    /** 报表页：展示占用率、部门预订数量和数据库审计日志。 */
    private Node reportsView() {
        VBox box = page();
        HBox actions = actions();
        Button occupancy = button("Occupancy by floor");
        Button department = button("Bookings by department");
        Button audit = button("Audit log");
        occupancy.setOnAction(e -> runQuery("Occupancy", "SELECT sd.building_name, sd.floor_number, COUNT(DISTINCT sd.seat_id) total_seats, SUM(CASE WHEN b.booking_id IS NULL THEN 0 ELSE 1 END) bookings_today FROM seat_directory sd LEFT JOIN bookings b ON sd.seat_id=b.seat_id AND b.booking_date=CURRENT_DATE() AND b.status='ACTIVE' GROUP BY sd.building_name, sd.floor_number ORDER BY sd.floor_number"));
        department.setOnAction(e -> runQuery("Department bookings", "SELECT d.department_name, COUNT(b.booking_id) active_bookings FROM departments d LEFT JOIN employees e ON d.department_id=e.department_id LEFT JOIN bookings b ON e.employee_id=b.employee_id AND b.status='ACTIVE' GROUP BY d.department_name ORDER BY active_bookings DESC"));
        audit.setOnAction(e -> runQuery("Audit", "SELECT action_time, entity_name, entity_id, action_name, details FROM audit_log ORDER BY action_time DESC LIMIT 50"));
        actions.getChildren().addAll(occupancy, department, audit);
        box.getChildren().add(actions);
        return box;
    }

    private VBox page() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));


        return box;
    }

    private HBox actions() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);
        return grid;
    }

    private Button button(String text) {
        Button button = new Button(text);
        button.setMinHeight(34);
        return button;
    }

    private String like(String text) {
        return "%" + (text == null ? "" : text.trim()) + "%";
    }

    /** 测试数据库连接，并把连接状态显示在窗口底部。 */
    private void testConnection() {
        try (Connection ignored = database.connection()) {
            status.setText("Connected to MySQL database");
        } catch (Exception ex) {
            status.setText("Database connection failed: " + ex.getMessage());
        }
    }

    /** 从 seat_directory 视图读取完整座位目录。 */
    private void loadSeats() {
        runQuery("Seats", "SELECT seat_code, seat_type, status, has_monitor, has_docking, zone_name, noise_level, building_name, floor_number FROM seat_directory ORDER BY seat_code");
    }

    /** 查询今天没有 ACTIVE 预订且状态为 AVAILABLE 的座位。 */
    private void loadAvailableSeats() {
        runQuery("Available seats", "SELECT seat_code, seat_type, zone_name, building_name, floor_number FROM seat_directory sd WHERE sd.status='AVAILABLE' AND NOT EXISTS (SELECT 1 FROM bookings b WHERE b.seat_id=sd.seat_id AND b.booking_date=CURRENT_DATE() AND b.status='ACTIVE') ORDER BY seat_code");
    }

    private void loadBookings() {
        runQuery("Bookings", "SELECT booking_id, booking_date, start_time, end_time, status, full_name, department_name, seat_code, purpose FROM booking_overview ORDER BY booking_date DESC, start_time DESC");
    }

    private void loadEmployees() {
        runQuery("Employees", "SELECT employee_no, full_name, email, role_title, work_mode, department_name FROM employees e JOIN departments d ON e.department_id=d.department_id ORDER BY full_name");
    }

    private void loadTickets() {
        runQuery("Tickets", "SELECT t.ticket_id, sd.seat_code, e.full_name reported_by, t.issue_title, t.priority, t.status, t.reported_at, t.resolved_at FROM maintenance_tickets t JOIN seat_directory sd ON t.seat_id=sd.seat_id JOIN employees e ON t.reported_by=e.employee_id ORDER BY t.reported_at DESC");
    }

    /** 创建座位预订。应用层会先检查时间格式和时间段冲突。 */
    private void createBooking() {
        Item employee = employeeBox.getValue();
        Item seat = seatBox.getValue();
        if (employee == null || seat == null || bookingDate.getValue() == null) {
            alert("Please select employee, seat and date.");
            return;
        }
        try {
            LocalTime start = LocalTime.parse(startTime.getText().trim());
            LocalTime end = LocalTime.parse(endTime.getText().trim());
            if (!start.isBefore(end)) {
                alert("Start time must be before end time.");
                return;
            }
            if (hasOverlap(seat.id(), bookingDate.getValue(), start, end)) {
                alert("This seat already has an active booking in the selected time range.");
                return;
            }
            try (Connection c = database.connection(); PreparedStatement ps = c.prepareStatement("INSERT INTO bookings(seat_id, employee_id, booking_date, start_time, end_time, purpose) VALUES (?,?,?,?,?,?)")) {
                ps.setInt(1, seat.id());
                ps.setInt(2, employee.id());
                ps.setDate(3, Date.valueOf(bookingDate.getValue()));
                ps.setTime(4, Time.valueOf(start));
                ps.setTime(5, Time.valueOf(end));
                ps.setString(6, purpose.getText().isBlank() ? "Desk booking" : purpose.getText().trim());
                ps.executeUpdate();
                status.setText("Booking created successfully");
                loadBookings();
            }
        } catch (Exception ex) {
            alert("Could not create booking: " + ex.getMessage());
        }
    }

    /** 检查同一个座位在同一天是否已有重叠的有效预订。 */
    private boolean hasOverlap(int seatId, LocalDate date, LocalTime start, LocalTime end) throws SQLException, IOException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE seat_id=? AND booking_date=? AND status='ACTIVE' AND start_time < ? AND end_time > ?";
        try (Connection c = database.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(end));
            ps.setTime(4, Time.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** 创建维护工单。数据库触发器会自动把对应座位标记为 MAINTENANCE。 */
    private void createTicket(Item seat, Item reporter, String title, String detail, String priority) {
        if (seat == null || reporter == null || title == null || title.isBlank() || detail == null || detail.isBlank()) {
            alert("Please complete the maintenance ticket form.");
            return;
        }
        try (Connection c = database.connection(); PreparedStatement ps = c.prepareStatement("INSERT INTO maintenance_tickets(seat_id, reported_by, issue_title, issue_detail, priority) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, seat.id());
            ps.setInt(2, reporter.id());
            ps.setString(3, title.trim());
            ps.setString(4, detail.trim());
            ps.setString(5, priority);
            ps.executeUpdate();
            status.setText("Maintenance ticket created");
            loadTickets();
        } catch (Exception ex) {
            alert("Could not create ticket: " + ex.getMessage());
        }
    }

    /** 刷新下拉框中的员工和可预订座位。 */
    private void refreshCombos() {
        employeeBox.setItems(items("SELECT employee_id, full_name FROM employees WHERE is_active=TRUE ORDER BY full_name"));
        seatBox.setItems(items("SELECT seat_id, seat_code FROM seats WHERE status='AVAILABLE' ORDER BY seat_code"));
    }

    /** 执行只返回 id/name 两列的查询，用于填充 ComboBox。 */
    private ObservableList<Item> items(String sql) {
        ObservableList<Item> values = FXCollections.observableArrayList();
        try (Connection c = database.connection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                values.add(new Item(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception ex) {
            status.setText("Could not load choices: " + ex.getMessage());
        }
        return values;
    }

    /** 通用查询方法：执行 SQL 并把结果显示到 TableView。 */
    private void runQuery(String label, String sql, String... params) {
        try (Connection c = database.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                fillTable(rs);
                status.setText(label + " loaded");
            }
        } catch (Exception ex) {
            status.setText(label + " failed: " + ex.getMessage());
            alert(label + " failed: " + ex.getMessage());
        }
    }

    /** 根据 ResultSet 的元数据动态生成表格列，并填充查询结果。 */
    private void fillTable(ResultSet rs) throws SQLException {
        table.getColumns().clear();
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            final int index = i - 1;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(meta.getColumnLabel(i));
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(index)));
            table.getColumns().add(column);
        }
        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object value = rs.getObject(i);
                row.add(value == null ? "" : value.toString());
            }
            rows.add(row);
        }
        table.setItems(rows);
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /** ComboBox 使用的小对象，保存数据库主键和显示名称。 */
    record Item(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    /** 数据库工具类：从 config/database.properties 读取连接参数。 */
    static class Database {
        /** 创建一个新的 MySQL 数据库连接。 */
        Connection connection() throws SQLException, IOException {
            Properties props = new Properties();
            try (FileInputStream input = new FileInputStream("config/database.properties")) {
                props.load(input);
            }
            String url = clean(props.getProperty("db.url"));
            String user = clean(props.getProperty("db.user"));
            String password = clean(props.getProperty("db.password"));
            if (url == null || url.isBlank()) {
                throw new SQLException("Database URL is missing. Please check config/database.properties.");
            }
            return DriverManager.getConnection(url, user, password);
        }

        private String clean(String value) {
            return value == null ? null : value.replace("\uFEFF", "").trim();
        }
    }
}







