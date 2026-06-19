# Momently - Mạng xã hội chia sẻ khoảnh khắc đồng bộ thời gian thực

## Chức năng cơ bản

| Yêu cầu                     | Chức năng trong app                                                                                                        |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **Layouts, Views**          | Màn hình Feed, Đăng bài, Bạn bè, Profile xây dựng bằng Jetpack Compose (`Scaffold`, `LazyColumn`, `Card`, `Row`, `Column`) |
| **Dialog / Toast**          | AlertDialog xác nhận xoá bài viết, Toast thông báo đăng bài thành công/thất bại                                            |
| **Menu**                    | Bottom Navigation gồm Feed - Đăng bài - Bạn bè - Profile                                                                   |
| **Intent**                  | Mở Camera chụp ảnh, mở Gallery chọn ảnh, mở màn hình chi tiết bài viết                                                     |
| **Service**                 | WorkManager đồng bộ danh sách bạn bè và làm mới dữ liệu nền                                                                |
| **Navigation**              | Navigation Compose quản lý điều hướng giữa các màn hình                                                                    |
| **Content Provider**        | Truy cập thư viện ảnh và danh bạ điện thoại (`ContactsContract`)                                                           |
| **Room Database**           | Lưu bản nháp bài viết và thông tin người dùng cục bộ                                                                       |
| **Web API** *(cộng điểm)*   | iTunes Search API tìm kiếm bài hát                                                                                         |
| **Animation** *(cộng điểm)* | AnimatedVisibility, Crossfade, hiệu ứng Like                                                                               |
| **Firebase** *(cộng điểm)*  | Firebase Authentication (Phone Auth), Firestore                                                                            |

---

## Chức năng chính

### 1. Đăng nhập bằng số điện thoại

* Nhập số điện thoại.
* Nhập OTP xác thực.
* Firebase Authentication xử lý đăng nhập.
* Tự động duy trì phiên đăng nhập.

---

### 2. Đăng bài

* Chụp ảnh bằng CameraX.
* Hoặc chọn ảnh từ thư viện.
* Nhập caption.
* Tìm kiếm bài hát bằng iTunes Search API.
* Đính kèm bài hát vào bài viết.
* Upload ảnh lên Supabase Storage.
* Lưu thông tin bài viết vào Firestore.

---

### 3. Feed bài viết

* Xem bài viết từ bạn bè.
* Hiển thị:

  * Ảnh bài viết
  * Caption
  * Tên bài hát
  * Nghệ sĩ
  * Thời gian đăng
* Like bài viết.
* Hỗ trợ xem offline với Firestore Cache và Coil Disk Cache.

---

### 4. Gợi ý bạn bè

* Đọc danh bạ điện thoại từ thiết bị.
* Lấy danh sách số điện thoại.
* So khớp với người dùng đã đăng ký trên hệ thống.
* Hiển thị danh sách "Có thể bạn biết".

---

### 5. Hồ sơ cá nhân

* Xem thông tin cá nhân.
* Xem danh sách bài viết đã đăng.
* Chỉnh sửa tên hiển thị.
* Xoá bài viết.

---

### 6. Bản nháp

* Tự động lưu khi đang soạn bài.
* Room lưu:

  * Caption
  * URI ảnh
  * Thông tin bài hát
* Khôi phục khi người dùng mở lại màn hình đăng bài.

---

### 7. Hỗ trợ ngoại tuyến (Offline)

* Firestore Offline Persistence lưu cache dữ liệu bài viết.
* Coil Disk Cache lưu cache ảnh đã xem.
* Room lưu dữ liệu cục bộ:

  * Thông tin người dùng.
  * Bản nháp bài viết.
* Người dùng vẫn có thể xem các bài viết và ảnh đã tải trước đó khi mất kết nối mạng.

---

## Công nghệ sử dụng

| Thành phần           | Công nghệ                     |
| -------------------- | ----------------------------- |
| UI                   | Jetpack Compose               |
| Navigation           | Navigation Compose            |
| State Management     | ViewModel + StateFlow         |
| Local Database       | Room                          |
| Authentication       | Firebase Phone Authentication |
| Cloud Database       | Firebase Firestore            |
| Storage              | Supabase Storage              |
| Music Search         | iTunes Search API             |
| Image Loading        | Coil Compose                  |
| Background Task      | WorkManager                   |
| Dependency Injection | Hilt *(nếu kịp)*              |
| Architecture         | MVVM                          |

---

## Cấu trúc dữ liệu

### User

```text
uid
phoneNumber
displayName
avatarUrl
createdAt
```

### Post

```text
postId
userId
caption
imageUrl
songName
artistName
previewUrl
createdAt
likeCount
```

### DraftPost (Room)

```text
id
imageUri
caption
songName
artistName
updatedAt
```

### UserProfile (Room)

```text
uid
phoneNumber
displayName
avatarUrl
```

---

## Cấu trúc màn hình

```text
LoginScreen

MainScreen
 ├── FeedScreen
 ├── CreatePostScreen
 ├── FriendsScreen
 └── ProfileScreen

PostDetailScreen
```

---

## Kiến trúc hệ thống

```text
Firebase Phone Auth
        │
        ▼
     Firestore
        │
        ├── Users
        ├── Posts
        └── Friends

Supabase Storage
        │
        └── Images

Room
        │
        ├── UserProfile
        └── DraftPost

iTunes API
        │
        └── Song Search

Jetpack Compose
        │
        ▼
       UI
```
