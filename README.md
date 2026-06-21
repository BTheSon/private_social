# Momently - Mạng xã hội chia sẻ khoảnh khắc đồng bộ thời gian thực

## Chức năng cơ bản

| Yêu cầu                 | Chức năng trong app                                                                  | File / Vị trí triển khai (Tên hàm, Class)                                                   |
|:------------------------|:-------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------|
| **Layouts, Views**      | Màn hình Feed, Đăng bài, Bạn bè, Profile xây dựng bằng Jetpack Compose               | `MainScreen.kt` (Hàm `MainScreen`), `ProfileScreen.kt`, `CameraScreen.kt`                   |
| **Dialog / Toast**      | AlertDialog xác nhận xoá bài viết/đăng xuất, Toast thông báo                         | `LogoutDialog.kt` (Hàm `LogoutDialog`), `DeletePhotoDialog.kt`, dùng `Toast.makeText`       |
| **Menu**                | Bottom Navigation gồm 3 tab: Máy ảnh (kết hợp Feed & Đăng bài) - Bạn bè - Profile    | `MainScreen.kt` (Phần Box chứa Row các `IconButton` ở cuối hàm)                             |
| **Intent**              | CameraX nhúng trực tiếp, chọn ảnh với `PickVisualMedia` thay cho Intent truyền thống | `CameraContent.kt` (`ActivityResultContracts.PickVisualMedia()`), `CameraViewfinderPage.kt` |
| **Service**             | WorkManager xử lý đăng bài nền (Background Upload) và tự động retry khi có mạng                                            | `PostUploadWorker.kt` (Class `PostUploadWorker`, hàm `doWork`)                          |
| **Navigation**          | Navigation Compose quản lý màn hình                                                  | `MainActivity.kt` (Class `MainActivity`, khối `NavHost`)                                    |
| **Content Provider**    | Truy cập danh bạ điện thoại (`ContactsContract`)                                     | `ContactsReader.kt` (Class `ContactsReader`, hàm `readContacts()`)                          |
| **Room Database**       | Lưu bản nháp và thông tin người dùng cục bộ                                          | `MDatabase.kt` (Class `MDatabase`, `DraftDao`, `UserDao`)                                   |
| **Web API** *(Bonus)*   | iTunes Search API tìm bài hát                                                        | `ItunesApiService.kt` (Hàm `searchSongs()`), `SongSearchDialog.kt`                          |
| **Animation** *(Bonus)* | `AnimatedVisibility`, `animateFloatAsState` (Scale hiệu ứng Camera)                  | `PendingPhotoConfirmationScreen.kt` (`AnimatedVisibility`), `CameraViewfinderPage.kt`       |
| **Firebase** *(Bonus)*  | Firebase Phone Auth, Firebase Realtime Database                                      | `AuthViewModel.kt` (`signInWithPhoneAuthCredential`), `FirebaseClientService.kt`            |

---

## Chức năng chính

### 1. Đăng nhập bằng số điện thoại

* Nhập số điện thoại.
* Nhập OTP xác thực.
* Firebase Authentication xử lý đăng nhập.
* Tự động duy trì phiên đăng nhập.

---

### 2. Đăng bài

* Chụp ảnh bằng CameraX nhúng trực tiếp.
* Hoặc chọn ảnh từ thư viện.
* Nhập caption.
* Tìm kiếm bài hát bằng iTunes Search API.
* Đính kèm bài hát vào bài viết.
* Upload ảnh lên Supabase Storage.
* Lưu thông tin bài viết vào Firebase Realtime Database.

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
* Hỗ trợ xem mượt mà với Coil Disk Cache.

---

### 4. Gợi ý bạn bè

* Đọc danh bạ điện thoại từ thiết bị.
* Lấy danh sách số điện thoại.
* So khớp với người dùng đã đăng ký trên hệ thống.
* Hiển thị danh sách "Có thể bạn biết".

---

### 5. Hồ sơ cá nhân

* Xem thông tin cá nhân.
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

### 7. Hỗ trợ ngoại tuyến (Offline) & Tác vụ nền

* Tự động đăng bài nền với **WorkManager**: Cho phép người dùng tắt app hoặc mất mạng, hệ thống tự động lưu vào hàng đợi và upload khi có mạng lại.
* Firebase Realtime Database cache dữ liệu cục bộ.
* Coil Disk Cache lưu cache ảnh đã xem.
* Room lưu dữ liệu cục bộ vững chắc:

  * Thông tin người dùng.
  * Bản nháp bài viết.
  * Thông tin bạn bè.
* Người dùng vẫn có thể xem các bài viết và ảnh đã tải trước đó khi mất kết nối mạng.

---

## Công nghệ sử dụng và File triển khai

| Thành phần       | Công nghệ                     | File / Vị trí triển khai chính                                                              |
|------------------|-------------------------------|---------------------------------------------------------------------------------------------|
| UI               | Jetpack Compose               | `MainScreen.kt`, `CameraScreen.kt`, `FriendScreen.kt`, `ProfileScreen.kt`                   |
| Navigation       | Navigation Compose            | `MainActivity.kt` (NavHost)                                                                 |
| State Management | ViewModel + StateFlow         | `PostViewModel.kt`, `AuthViewModel.kt`, `PhotoViewModel.kt`, `ProfileViewModel.kt`          |
| Local Database   | Room                          | `MDatabase.kt`, `DraftDao.kt`, `UserDao.kt`, `FriendDao.kt`, `PhotoDao.kt`                  |
| Authentication   | Firebase Phone Authentication | `FirebaseClientService.kt`, `AuthScreen.kt`                                                 |
| Cloud Database   | Realtime Database             | `FirebaseClientService.kt`, `PostRepository.kt`, `UserRepository.kt`, `FriendRepository.kt` |
| Storage          | Supabase Storage              | `SupabaseClientService.kt`, `PostRepository.kt`                                             |
| Music Search     | iTunes Search API             | `ItunesApiService.kt`, `SongSearchDialog.kt`                                                |
| Image Loading    | Coil Compose                  | `TimelinePhotoItem.kt`, `GalleryScreen.kt`, `PendingPhotoConfirmationScreen.kt`             |
| Background Task  | WorkManager                   | `SyncContactsWorker.kt`                                                                     |
| Content Provider | ContactsContract              | `ContactsReader.kt`, `ContactProvider.kt`                                                   |
| Architecture     | MVVM                          | Kiến trúc chia tách rõ ràng 2 package `backend/domain` và `frontend/screens`                |

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
 ├── CameraScreen (gộp chung tính năng Feed và Đăng bài)
 ├── FriendScreen
 └── ProfileScreen

GalleryScreen
```

---

## Kiến trúc hệ thống

```text
Firebase Phone Auth
        │
        ▼
  Realtime Database
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
        ├── Friends
        └── DraftPost

iTunes API
        │
        └── Song Search

Jetpack Compose
        │
        ▼
       UI
```
