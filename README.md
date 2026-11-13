# HỆ THỐNG QUẢN LÝ ĐƠN HÀNG (ORDER MANAGEMENT)

Ứng dụng web hoàn chỉnh theo đúng tài liệu **"MÔ TẢ CÁC BƯỚC XÂY DỰNG CƠ SỞ DỮ LIỆU QUẢN LÝ ĐƠN HÀNG"**.

---

## 🏗️ 1. Thiết Kế Cơ Sở Dữ Liệu (`order_management`)

Cơ sở dữ liệu hỗ trợ cả **MySQL** và **H2 (chế độ tương thích MySQL)**. Toàn bộ câu lệnh SQL tạo bảng và khóa ngoại có sẵn tại file [database_schema.sql](file:///f:/Lay%20Don/database_schema.sql).

### 2.1. Bảng `users` – Tài khoản người dùng
| Tên trường | Kiểu dữ liệu / ràng buộc | Mô tả |
|---|---|---|
| `id` | INT, PK, AUTO_INCREMENT | Mã tài khoản |
| `name` | VARCHAR(100), NOT NULL, UNIQUE | Tên đăng nhập |
| `password` | VARCHAR(255), NOT NULL | Mật khẩu băm bằng BCrypt |
| `role` | ENUM('admin','user') | Vai trò tài khoản |

### 2.2. Bảng `code_values` – Giá trị mã
| Tên trường | Kiểu dữ liệu / ràng buộc | Mô tả |
|---|---|---|
| `id` | INT, PK, AUTO_INCREMENT | Mã định danh |
| `code_value` | VARCHAR(255), UNIQUE, NOT NULL | Nội dung giá trị mã (ví dụ: `125`, `1065`, `6677`...) |

### 2.3. Bảng `orders` – Đơn hàng
| Tên trường | Kiểu dữ liệu / ràng buộc | Mô tả |
|---|---|---|
| `id` | INT, PK, AUTO_INCREMENT | Mã định danh đơn hàng |
| `user_id` | INT, FK → users.id | Tài khoản tạo đơn |
| `order_code` | VARCHAR(100), UNIQUE, NOT NULL | Mã đơn hàng (ví dụ: `DH001`) |
| `created_at` | DATETIME | Ngày giờ tạo đơn |
| `sent_at` | DATETIME, NULL | Ngày giờ gửi hàng |
| `status` | ENUM('da_tao','da_dat','da_nhan_hang') | Trạng thái đơn hàng |

### 2.4. Bảng `order_details` – Chi tiết đơn hàng
| Tên trường | Kiểu dữ liệu / ràng buộc | Mô tả |
|---|---|---|
| `id` | INT, PK, AUTO_INCREMENT | Mã dòng chi tiết |
| `order_id` | INT, FK → orders.id (CASCADE DELETE) | Đơn hàng chứa sản phẩm |
| `product_name` | VARCHAR(255), NOT NULL | Tên sản phẩm |
| `variant` | VARCHAR(255), NULL | Phân loại sản phẩm (ví dụ: `4GB/128GB`) |
| `quantity` | INT, NOT NULL (> 0) | Số lượng sản phẩm |
| `code_value_id` | INT, FK → code_values.id (RESTRICT DELETE) | Giá trị mã được chọn |

---

## 🔗 3. Quy Tắc Nghiệp Vụ & Ràng Buộc Đã Triển Khai
1. **Chỉ Đăng Nhập**: Trang đăng nhập (`/login.html`) chỉ có form login bằng `name` và `password`.
2. **Tạo tài khoản độc quyền ở Admin**: Người dùng không thể tự đăng ký tự do; Admin tạo tài khoản tại tab "Tài Khoản" trên trang Admin.
3. **Giá trị mã duy nhất**: Không cho tạo 2 mã trùng lặp trong `code_values`.
4. **Bảo vệ mã đang dùng**: Không cho xóa giá trị mã nếu đang có sản phẩm trong đơn hàng tham chiếu đến.
5. **Đơn hàng có nhiều sản phẩm**: Người dùng có thể thêm/bớt nhiều dòng sản phẩm vào 1 đơn hàng khi tạo.
6. **Xóa đơn hàng thì chi tiết tự xóa theo**: Ràng buộc Cascade Delete đảm bảo khi xóa đơn hàng thì các dòng `order_details` được xóa sạch.

---

## 🔑 Tài Khoản Mặc Định

| Tên đăng nhập (`name`) | Mật khẩu | Quyền | Mô tả |
|---|---|---|---|
| `admin` | `admin123` | `admin` | Quản trị viên: Quản lý đơn hàng, đổi trạng thái, quản lý mã, tạo tài khoản |
| `user` | `user123` | `user` | Người dùng: Tạo đơn hàng với nhiều sản phẩm, theo dõi đơn hàng |

---

## 🚀 Cách Khởi Chạy

### Cách 1: Click file `run.bat`
Click đúp chuột vào [run.bat](file:///f:/Lay%20Don/run.bat) để chạy ứng dụng.

### Cách 2: Chạy lệnh qua PowerShell / CMD
```powershell
java -jar target\auth-app-0.0.1-SNAPSHOT.jar
```

Mở trình duyệt: 👉 **[http://localhost:8080](http://localhost:8080)**

*(Nếu muốn kết nối trực tiếp MySQL Laragon/XAMPP: Chạy với tham số `--spring.profiles.active=mysql`)*
