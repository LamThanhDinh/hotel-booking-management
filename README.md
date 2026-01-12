# 🏨 Hệ Thống Quản Lý Khách Sạn (Hotel Management System)

Ứng dụng quản lý khách sạn toàn diện được xây dựng bằng Java Swing với kiến trúc Clean Architecture.

## 📋 Mô Tả

Hệ thống quản lý khách sạn cung cấp các chức năng:

- **🛏️ Quản lý Phòng**: Theo dõi trạng thái phòng, loại phòng, giá cả
- **📅 Quản lý Đặt Phòng**: Tạo booking mới, quản lý khách hàng, kiểm tra tình trạng đặt phòng
- **🍽️ Quản lý Dịch Vụ**: Thêm dịch vụ cho khách (đồ ăn, giặt ủi, spa, v.v.)
- **💳 Thanh Toán**: Tính toán hóa đơn, checkout khách
- **📊 Báo Cáo Doanh Thu**: Thống kê doanh thu theo thời gian

## 🛠️ Công Nghệ Sử Dụng

- **Java 21**: Ngôn ngữ lập trình chính
- **Maven 3.9+**: Quản lý dependencies và build tool
- **MySQL 8+**: Cơ sở dữ liệu
- **Java Swing**: Framework GUI
- **FlatLaf 3.4**: Modern Look & Feel cho giao diện
- **MySQL Connector/J 8.3**: JDBC driver

## 📦 Yêu Cầu Hệ Thống

### Phần Mềm Cần Thiết

1. **Java Development Kit (JDK) 21**
   - Tải về: https://www.oracle.com/java/technologies/downloads/#java21
   - Hoặc: https://adoptium.net/

2. **Apache Maven 3.9+**
   - Tải về: https://maven.apache.org/download.cgi
   - Bạn đã có tại: `D:\apache-maven-3.9.12-bin\apache-maven-3.9.12`

3. **MySQL Server 8.0+**
   - Tải về: https://dev.mysql.com/downloads/mysql/

## ⚙️ Cài Đặt và Cấu Hình

### Bước 1: Cấu Hình Maven PATH

Vì Maven của bạn chưa có trong PATH, hãy thêm vào biến môi trường:

#### Cách 1: Tạm thời (cho phiên PowerShell hiện tại)
```powershell
$env:Path += ";D:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin"
```

#### Cách 2: Vĩnh viễn (khuyến nghị)
1. Mở **System Properties**:
   - Nhấn `Windows + R`
   - Gõ `sysdm.cpl` và nhấn Enter

2. Chọn tab **Advanced** → Click **Environment Variables**

3. Trong **System variables**, tìm biến `Path`:
   - Click **Edit**
   - Click **New**
   - Thêm: `D:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin`
   - Click **OK** tất cả các cửa sổ

4. Mở PowerShell mới và kiểm tra:
```powershell
mvn -version
```

### Bước 2: Cấu Hình MySQL Database

1. **Khởi động MySQL Server** (port 3307 theo config của bạn)

2. **Tạo Database và Tables**:
```powershell
# Kết nối vào MySQL
mysql -u root -P 3307

# Trong MySQL prompt, chạy:
source D:/QuanLiKhachSan/qlks/schema.sql
source D:/QuanLiKhachSan/qlks/seed.sql
```

Hoặc sử dụng MySQL Workbench để import file `schema.sql` và `seed.sql`.

3. **Cấu hình kết nối** trong file [qlks/app.properties](qlks/app.properties):
```properties
db.enabled=true
db.url=jdbc:mysql://localhost:3307/hotel_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_password_here
db.poolSize=5
```

⚠️ **Lưu ý**: Thay `your_password_here` bằng mật khẩu MySQL của bạn.

### Bước 3: Build Project

Di chuyển vào thư mục project và build:

```powershell
cd D:\QuanLiKhachSan\qlks
mvn clean package
```

Nếu build thành công, file JAR sẽ được tạo tại: `target/qlks-1.0.0.jar`

## 🚀 Chạy Ứng Dụng

### Cách 1: Chạy trực tiếp bằng Maven (khuyến nghị)
```powershell
cd D:\QuanLiKhachSan\qlks
mvn clean compile exec:java -D exec.mainClass=com.hotel.app.MainApplication
```

**Lưu ý**: Trong PowerShell, KHÔNG dùng dấu ngoặc kép cho tham số `-D`

### Cách 2: Chạy từ file JAR đã build
```powershell
cd D:\QuanLiKhachSan\qlks
mvn clean package
java -jar target/qlks-1.0.0.jar
```

**Lưu ý**: Nếu gặp lỗi "no main manifest attribute", cần cấu hình `maven-jar-plugin` trong `pom.xml`

### Cách 3: Chạy trong IDE (VS Code, IntelliJ, Eclipse)
1. Mở project trong IDE
2. Tìm file [src/com/hotel/app/MainApplication.java](qlks/src/com/hotel/app/MainApplication.java)
3. Click chuột phải → **Run 'MainApplication.main()'**

## 📁 Cấu Trúc Project

```
qlks/
├── src/com/hotel/
│   ├── app/              # Application entry point & composition root
│   ├── booking/          # Module đặt phòng
│   │   ├── application/  # Use cases & business logic
│   │   ├── data/         # Repositories implementation
│   │   ├── domain/       # Entities & value objects
│   │   └── ui/          # Swing panels
│   ├── rooms/           # Module quản lý phòng
│   ├── services/        # Module dịch vụ
│   ├── checkout/        # Module thanh toán
│   └── revenue/         # Module báo cáo doanh thu
├── schema.sql          # Database schema
├── seed.sql           # Sample data
├── app.properties     # Database configuration
└── pom.xml           # Maven configuration
```

## 🎯 Các Chức Năng Chính

### 1. Quản Lý Phòng (Rooms)
- Xem danh sách phòng theo trạng thái (Available, Occupied, Cleaning)
- Thêm/sửa/xóa thông tin phòng
- Cập nhật trạng thái phòng

### 2. Đặt Phòng (Booking)
- Tạo booking mới cho khách hàng
- Quản lý thông tin khách (họ tên, SĐT, CMND/CCCD)
- Kiểm tra phòng trống theo ngày
- Cập nhật/hủy booking

### 3. Dịch Vụ (Services)
- Thêm dịch vụ cho booking
- Các loại dịch vụ: Đồ ăn, Giặt ủi, Spa, Đưa đón sân bay, v.v.
- Theo dõi chi phí dịch vụ

### 4. Thanh Toán (Checkout)
- Xem danh sách booking đang active
- Tính toán tổng chi phí (phòng + dịch vụ)
- In hóa đơn
- Checkout và cập nhật trạng thái phòng

### 5. Báo Cáo Doanh Thu (Revenue)
- Thống kê doanh thu theo ngày/tháng/năm
- Biểu đồ trực quan
- Xuất báo cáo

## 🐛 Xử Lý Lỗi Thường Gặp

### Lỗi: "mvn command not found"
**Nguyên nhân**: Maven chưa có trong PATH

**Giải pháp**: Làm theo Bước 1 ở phần Cài Đặt

### Lỗi: "Communications link failure"
**Nguyên nhân**: Không kết nối được MySQL

**Giải pháp**:
- Kiểm tra MySQL Server đã chạy chưa
- Xác nhận port trong `app.properties` (mặc định: 3307)
- Kiểm tra username/password

### Lỗi: "Table 'hotel_management.xxx' doesn't exist"
**Nguyên nhân**: Database chưa được tạo

**Giải pháp**: Chạy lại `schema.sql`

## 👨‍💻 Development

### Build và Test
```powershell
# Clean và compile
mvn clean compile

# Chạy tests (nếu có)
mvn test

# Package thành JAR
mvn package

# Clean target folder
mvn clean
```

## 📝 License

Project này được phát triển cho mục đích học tập.

## 📧 Liên Hệ

Nếu có vấn đề hoặc câu hỏi, vui lòng tạo issue trong repository.
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
