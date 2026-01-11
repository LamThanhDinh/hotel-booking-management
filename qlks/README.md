## Quản lý khách sạn (Swing + Maven)

Ứng dụng desktop quản lý khách sạn: đặt phòng, gán dịch vụ, checkout, báo cáo doanh thu. Giao diện Swing (FlatLaf). Chạy in-memory hoặc MySQL tùy cấu hình.

## Tính năng chính
- Quản lý phòng: xem danh sách, chi tiết, đổi trạng thái.
- Đặt phòng: lưu thông tin khách, khoảng thời gian.
- Dịch vụ: gán dịch vụ cho đặt phòng, tính phí dịch vụ.
- Thanh toán: tính hóa đơn, checkout, lưu invoice.
- Doanh thu: tổng hợp theo hóa đơn đã thanh toán.

## Kiến trúc tóm tắt
- Modules `rooms|booking|services|checkout|revenue`: tách domain, use case, UI Swing.
- `common`: kết nối DB, transaction, hạ tầng.
- `AppCompositionRoot`: bật/tắt MySQL qua `app.properties`, khởi tạo dependency.
- Entry point: `com.hotel.app.MainApplication` tạo `MainFrame` chứa các tab.

## Yêu cầu môi trường
- JDK 21
- Maven 3.9+
- (Tùy chọn) MySQL 8.x nếu dùng DB

## Cấu hình dữ liệu
- File cấu hình: `app.properties`
- Dùng MySQL: `db.enabled=true`, chỉnh `db.url`, `db.username`, `db.password` (mặc định cổng 3307 trong repo).
- Dùng in-memory: `db.enabled=false` (dữ liệu mất khi thoát ứng dụng).
- Lược đồ và seed: `schema.sql`, `seed.sql` (chạy khi dùng MySQL).

## Cách chạy (đã kiểm chứng)

### 1) Kiểm tra công cụ
- `java -version` (yêu cầu 21)
- `mvn -version` (yêu cầu 3.9+)

### 2) Chọn chế độ dữ liệu
- MySQL thật: giữ `db.enabled=true`, chỉnh `db.url`, `db.username`, `db.password`.
- Thử nhanh in-memory: đặt `db.enabled=false`, không cần MySQL.

### 3) Chuẩn bị MySQL (khi `db.enabled=true`)
- Tạo database, ví dụ `hotel_management`.
- Nạp schema:
  - PowerShell: `Get-Content schema.sql | mysql -u <user> -p<pass> hotel_management`
- Nạp seed:
  - PowerShell: `Get-Content seed.sql | mysql -u <user> -p<pass> hotel_management`
- Đảm bảo cổng khớp `db.url` (repo mặc định 3307).

### 4) Chạy bằng Maven (cách nhanh nhất)
- Cài dependency + biên dịch: `mvn clean compile`
- Chạy app: `mvn exec:java -Dexec.mainClass="com.hotel.app.MainApplication"`
  - PowerShell dùng trực tiếp; CMD cần đảm bảo dấu ngoặc kép quanh giá trị `-Dexec.mainClass`.
  - Maven sẽ tự tải plugin `exec` nếu chưa có.

### 5) Chạy in-memory (không MySQL)
- Sửa `app.properties`: `db.enabled=false`
- `mvn clean compile`
- `mvn exec:java -Dexec.mainClass="com.hotel.app.MainApplication"`

### 6) Đóng gói và chạy bằng jar (có đủ dependency)
- Đóng gói: `mvn clean package`
- Tải dependency ra thư mục: `mvn dependency:copy-dependencies -DoutputDirectory=target/dependency`
- Chạy bằng PowerShell (classpath gồm jar + dependency):
  - `java -cp "target\qlks-1.0.0.jar;target\dependency\*" com.hotel.app.MainApplication`
- Trên CMD cần giữ dấu ngoặc kép tương tự; trên Linux/Mac thay `;` bằng `:`.

### 7) (Tuỳ chọn) Tạo fat-jar
- Chưa cấu hình sẵn plugin đóng gói fat-jar. Nếu cần, thêm `maven-shade-plugin` vào `pom.xml` hoặc tiếp tục chạy bằng Maven/Classpath như trên.

## Cấu trúc thư mục
- `src/`: mã nguồn
- `lib/`: thư viện thủ công (nếu có)
- `bin/`: output VS Code
- `target/`: output Maven (jar, dependency)
