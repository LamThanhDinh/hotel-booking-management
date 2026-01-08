USE hotel_management;

-- Rooms
INSERT INTO rooms (room_id, room_name, room_type, bed_type, price_amount, currency, status)
VALUES
 ('R101','Room 101','STANDARD','SINGLE',450000,'VND','TRONG'),
 ('R102','Room 102','STANDARD','DOUBLE',500000,'VND','DA_DAT'),
 ('R201','Room 201','DELUXE','QUEEN',700000,'VND','DANG_SU_DUNG'),
 ('R202','Room 202','DELUXE','KING',850000,'VND','DA_TRA'),
 ('R301','Room 301','SUITE','KING',1200000,'VND','TRONG'),
 ('R302','Room 302','SUITE','KING',1250000,'VND','TRONG'),
 ('R303','Room 303','SUITE','QUEEN',1000000,'VND','DA_DAT'),
 ('R304','Room 304','DELUXE','DOUBLE',750000,'VND','DANG_SU_DUNG'),
 ('R401','Room 401','STANDARD','SINGLE',430000,'VND','TRONG'),
 ('R402','Room 402','STANDARD','DOUBLE',520000,'VND','TRONG');

-- Customers
INSERT INTO customers (customer_id, full_name, phone, identity_no)
VALUES
 ('CUST-1001','Nguyen Van A','0900000001','ID1001'),
 ('CUST-1002','Nguyen Van B','0900000002','ID1002'),
 ('CUST-1003','Tran Thi C','0900000003','ID1003'),
 ('CUST-1004','Le Van D','0900000004','ID1004'),
 ('CUST-1005','Pham Thi E','0900000005','ID1005');

-- Bookings (2 ACTIVE)
INSERT INTO bookings (booking_id, room_id, customer_id, check_in_date, check_out_date, status)
VALUES
 ('BKG-2001','R102','CUST-1001', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'ACTIVE'),
 ('BKG-2002','R303','CUST-1002', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'ACTIVE');

-- Services (8 total, 2 with stock=0)
INSERT INTO services (service_id, name, unit_price_amount, currency, stock, is_active)
VALUES
 ('SV01','Laundry',50000,'VND',10,1),
 ('SV02','Breakfast',80000,'VND',20,1),
 ('SV03','Airport Pickup',200000,'VND',5,1),
 ('SV04','Spa Package',300000,'VND',3,1),
 ('SV05','Mini Bar',120000,'VND',15,1),
 ('SV06','Extra Bed',150000,'VND',0,1),
 ('SV07','City Tour',400000,'VND',0,1),
 ('SV08','Late Checkout',100000,'VND',7,1);

-- Service usages (attach to BKG-2001)
INSERT INTO service_usages (usage_id, booking_id, service_id, quantity, used_at)
VALUES
 ('USG-3001','BKG-2001','SV01',2, NOW()),
 ('USG-3002','BKG-2001','SV02',2, NOW()),
 ('USG-3003','BKG-2001','SV05',1, NOW());

-- Invoice PAID for BKG-2001 (assume totals precomputed for seed)
INSERT INTO invoices (invoice_id, booking_id, room_total_amount, services_total_amount, grand_total_amount, currency, status, paid_at)
VALUES
 ('INV-4001','BKG-2001', 1500000, 280000, 1780000, 'VND', 'PAID', NOW());
