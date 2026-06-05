# Momently - Mạng xã hội chia sẻ khoảng khắc realtime

## Giới thiệu

Locket Clone là ứng dụng mạng xã hội chia sẻ ảnh được phát triển trên nền tảng Android bằng Jetpack Compose. Ứng dụng cho phép người dùng chụp và chia sẻ nhanh những khoảnh khắc hằng ngày với bạn bè, đồng thời tương tác thông qua các cảm xúc (reactions) và tin nhắn riêng tư.

Dự án được xây dựng nhằm áp dụng các kiến thức về phát triển ứng dụng Android hiện đại, quản lý dữ liệu cục bộ, điều hướng giao diện, dịch vụ nền và tích hợp các nền tảng điện toán đám mây.

---

## Chức năng chính

### Quản lý tài khoản

* Đăng ký tài khoản bằng SĐT/Password
* Đăng nhập hệ thống
* Đăng xuất
* Chỉnh sửa hồ sơ cá nhân
* Cập nhật ảnh đại diện

### Chia sẻ khoảnh khắc

* Chụp ảnh bằng camera
* Chọn ảnh từ thư viện thiết bị
* Đăng ảnh kèm mô tả ngắn hoặc kèm nhạc
* Lưu trữ ảnh trên cloud storage
* Xóa bài đăng của bản thân

### Bảng tin bạn bè

* Hiển thị ảnh mới nhất từ bạn bè
* Tải dữ liệu theo thời gian thực
* Làm mới dữ liệu bằng thao tác kéo xuống
* Xem thông tin người đăng và thời gian đăng
* Hiển thị list bạn bè gợi ý thông qua số số điện thoại trên máy người dùng (nếu sđt đó đã đăng kí app)

### Tương tác bài đăng

* Thả cảm xúc (❤️ 😂 😮 😢 👍)
* Xem số lượng phản hồi của bài đăng
* Nhận thông báo khi có người tương tác

### Tin nhắn cá nhân

* Chat 1-1 giữa bạn bè
* Gửi tin nhắn văn bản
* Chia sẻ (Reply) bài đăng trực tiếp vào cuộc trò chuyện

### Quản lý bạn bè

* Tìm kiếm người dùng
* Gửi lời mời kết bạn
* Chấp nhận hoặc từ chối lời mời
* Xem danh sách bạn bè

### Đồng bộ dữ liệu

* Cache dữ liệu bằng Room Database
* Hỗ trợ xem dữ liệu đã tải khi mất kết nối mạng
* Đồng bộ dữ liệu khi có kết nối mạng trở lại

---

## Các yêu cầu môn học được áp dụng

| Nội dung         | Áp dụng trong dự án                                   |
|------------------|-------------------------------------------------------|
| Layouts, Views   | Jetpack Compose UI                                    |
| Dialog/Toast     | Xóa bài đăng, xác nhận thao tác, thông báo trạng thái |
| Menu             | Bottom Navigation, Dropdown Menu                      |
| Intent           | Camera, Gallery, Chia sẻ nội dung                     |
| Service          | Upload ảnh nền và đồng bộ dữ liệu                     |
| Navigation       | Navigation Compose                                    |
| Content Provider | Đọc danh bạ để tìm bạn bè                             |
| Room Database    | Cache dữ liệu người dùng, bài đăng và tin nhắn        |

---

## Công nghệ sử dụng

### Android

* Kotlin
* Jetpack Compose
* Material Design 3
* Navigation Compose
* ViewModel
* StateFlow

### Local Storage

* Room Database
* DataStore Preferences

### Backend Services

#### Firebase

* Firebase Authentication
* Cloud Firestore
* Firebase Cloud Messaging (FCM)

#### Supabase

* Supabase Storage

### Networking

* Retrofit / Ktor Client
* Kotlin Coroutines
* Kotlin Flow

### Dependency Injection

* Hilt

---

## Kiến trúc hệ thống

Dự án được xây dựng theo mô hình MVVM (Model - View - ViewModel).

```text
Presentation Layer
        │
        ▼
ViewModel Layer
        │
        ▼
Repository Layer
   ┌─────────────┬─────────────┬─────────────┐
   ▼             ▼             ▼
Firestore    Supabase      Room DB
(Auth/Data)  (Storage)     (Cache)
```

---

## Luồng đăng ảnh

```text
Người dùng chụp ảnh
        │
        ▼
Upload ảnh lên Supabase Storage
        │
        ▼
Nhận URL ảnh
        │
        ▼
Lưu metadata vào Firestore
        │
        ▼
Đồng bộ lên Feed bạn bè
```

---

## Luồng reply bài đăng vào tin nhắn

```text
Người dùng chọn bài đăng
        │
        ▼
Nhấn "gửi tin nhắn ..."
        │
        ▼
Nhập tin nhắn muốn rep bài đăng
        │
        ▼
bài đăng kèm tin nhắn được gửi trực tiếp vào hộp thoai
```

---

## Hướng phát triển

* Android Home Screen Widget
* Chat nhóm
* Story 24 giờ
* Chia sẻ video ngắn
* Realtime Presence (Online/Offline)
* AI phân loại ảnh tự động

---

## Thành viên nhóm

* Thành viên 1: ...
* Thành viên 2: ...
* Thành viên 3: ...

## Giảng viên hướng dẫn

* ...
