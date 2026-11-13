# HƯỚNG DẪN TRIỂN KHAI (DEPLOY) LÊN CLOUD MIỄN PHÍ

Dưới đây là 2 cách triển khai đơn giản, hoàn toàn miễn phí, có sẵn tên miền HTTPS công khai để người khác có thể truy cập qua Internet từ điện thoại hoặc máy tính khác.

---

## CÁCH 1: TRIỂN KHAI LÊN RENDER.COM (KHUYÊN DÙNG - DỄ NHẤT)

Render hỗ trợ build Docker tự động từ GitHub và cấp miễn phí tên miền dạng `https://ten-ung-dung.onrender.com`.

### Bước 1: Đẩy mã nguồn lên GitHub
Mở PowerShell hoặc Command Prompt tại thư mục dự án (`F:\Lay Don`) và chạy các lệnh:
```bash
git init
git add .
git commit -m "Deploy Order Management App"
git branch -M main
```

Sau đó lên [https://github.com](https://github.com) tạo một Repository mới (đặt tên ví dụ: `order-management`), rồi chạy lệnh liên kết và đẩy code lên:
```bash
git remote add origin https://github.com/<tai-khoan-github-cua-ban>/order-management.git
git push -u origin main
```

### Bước 2: Tạo Web Service trên Render
1. Truy cập [https://render.com](https://render.com) và đăng ký/đăng nhập bằng tài khoản GitHub.
2. Bấm nút **New +** ở góc trên bên phải $\rightarrow$ chọn **Web Service**.
3. Chọn Repository `order-management` bạn vừa đẩy lên.
4. Điền các thông tin:
   - **Name**: Nhập tên ứng dụng bạn thích (ví dụ: `quan-ly-don-hang`).
   - **Region**: Chọn `Singapore` (để tốc độ tải từ Việt Nam nhanh nhất).
   - **Language / Runtime**: Render sẽ tự động nhận diện **Docker** từ file `Dockerfile` đã tạo sẵn.
   - **Instance Type**: Chọn gói **Free** (0$/tháng).
5. Bấm nút **Create Web Service**.

> Render sẽ tự động tải source code, build Docker và khởi chạy. Sau khoảng 2 - 3 phút, bạn sẽ nhận được một đường link HTTPS trực tiếp (ví dụ: `https://quan-ly-don-hang.onrender.com`).

---

## CÁCH 2: TRIỂN KHAI LÊN RAILWAY.APP

Railway có ưu điểm là build rất nhanh và hỗ trợ sẵn cả MySQL nếu bạn muốn dùng cơ sở dữ liệu MySQL thay cho H2.

### Các bước thực hiện:
1. Đẩy code lên GitHub như ở Bước 1 của Render.
2. Truy cập [https://railway.app](https://railway.app) $\rightarrow$ Đăng nhập bằng GitHub.
3. Chọn **New Project** $\rightarrow$ **Deploy from GitHub repo**.
4. Chọn repo `order-management`.
5. Railway sẽ tự động build từ file `Dockerfile`.
6. Sau khi deploy xong, vào tab **Settings** $\rightarrow$ mục **Networking** $\rightarrow$ bấm **Generate Domain** để nhận link truy cập công khai dạng `https://order-management-production.up.railway.app`.

---

## CÁCH 3: CHẠY BẰNG DOCKER TRÊN MÁY TÍNH CÁ NHÂN / VPS

Nếu bạn đã cài Docker Desktop trên máy:
```bash
docker compose up -d
```
Ứng dụng sẽ tự động khởi chạy và bạn có thể truy cập tại `http://localhost:8080`.
