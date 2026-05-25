USE office_seat_manager;

INSERT INTO departments(department_name, cost_centre) VALUES
('Engineering','ENG-100'),
('Human Resources','HR-200'),
('Finance','FIN-300'),
('Marketing','MKT-400');

INSERT INTO employees(employee_no, full_name, email, role_title, work_mode, department_id) VALUES
('E001','Alice Zhang','alice.zhang@example.com','Backend Developer','HYBRID',1),
('E002','Brian Chen','brian.chen@example.com','QA Engineer','ONSITE',1),
('E003','Clara Wang','clara.wang@example.com','HR Specialist','HYBRID',2),
('E004','Daniel Li','daniel.li@example.com','Accountant','ONSITE',3),
('E005','Eva Liu','eva.liu@example.com','Marketing Analyst','REMOTE',4);

INSERT INTO floors(building_name, floor_number, floor_name) VALUES
('Innovation Tower',1,'Client Floor'),
('Innovation Tower',2,'Product Floor'),
('Innovation Tower',3,'Operations Floor');

INSERT INTO zones(floor_id, zone_name, noise_level) VALUES
(1,'A - Quiet Focus','QUIET'),
(1,'B - Visitor Collaboration','COLLABORATIVE'),
(2,'C - Engineering','NORMAL'),
(2,'D - Pair Programming','COLLABORATIVE'),
(3,'E - Finance and HR','QUIET');

INSERT INTO seats(seat_code, zone_id, seat_type, has_monitor, has_docking, status) VALUES
('1A-01',1,'STANDARD',TRUE,TRUE,'AVAILABLE'),
('1A-02',1,'ACCESSIBLE',TRUE,TRUE,'AVAILABLE'),
('1B-01',2,'MEETING_POD',TRUE,FALSE,'AVAILABLE'),
('2C-01',3,'STANDARD',TRUE,TRUE,'AVAILABLE'),
('2C-02',3,'STANDING',TRUE,TRUE,'AVAILABLE'),
('2D-01',4,'STANDARD',FALSE,TRUE,'AVAILABLE'),
('3E-01',5,'STANDARD',TRUE,TRUE,'AVAILABLE'),
('3E-02',5,'STANDARD',FALSE,FALSE,'MAINTENANCE');

INSERT INTO bookings(seat_id, employee_id, booking_date, start_time, end_time, purpose) VALUES
(1,1,CURRENT_DATE(),'09:00','12:00','Morning focus work'),
(4,2,CURRENT_DATE(),'13:00','17:00','Testing sprint'),
(7,4,DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY),'09:30','16:30','Monthly closing');

INSERT INTO maintenance_tickets(seat_id, reported_by, issue_title, issue_detail, priority) VALUES
(8,3,'Loose power socket','The power socket beside seat 3E-02 is unstable and should not be used.','HIGH');
