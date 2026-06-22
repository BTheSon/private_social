# Momently - Mạng xã hội chia sẻ khoảnh khắc đồng bộ thời gian thực

## Chức năng cơ bản

| Yêu cầu                 | Chức năng trong app                                                                  | File / Vị trí triển khai (Tên hàm, Class)                                                   |
|:------------------------|:-------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------|
| **Layouts, Views**      | Màn hình Feed, Đăng bài, Bạn bè, Profile xây dựng bằng Jetpack Compose               | `MainScreen.kt` (Hàm `MainScreen`), `ProfileScreen.kt`, `CameraScreen.kt`                   |
| **Dialog / Toast**      | AlertDialog xác nhận xoá bài viết/đăng xuất, Toast thông báo                         | `LogoutDialog.kt` (Hàm `LogoutDialog`), `DeletePhotoDialog.kt`, dùng `Toast.makeText`       |
| **Menu**                | Bottom Navigation gồm 3 tab: Máy ảnh (kết hợp Feed & Đăng bài) - Bạn bè - Profile    | `MainScreen.kt` (Phần Box chứa Row các `IconButton` ở cuối hàm)                             |
| **Intent**              | CameraX nhúng trực tiếp, chọn ảnh với `PickVisualMedia` thay cho Intent truyền thống | `CameraContent.kt` (`ActivityResultContracts.PickVisualMedia()`), `CameraViewfinderPage.kt` |
| **WorkManager**         | Xử lý đăng bài ngầm và duy trì tác vụ upload ổn định bằng WorkManager                                          | `PostUploadWorker.kt` (Class `PostUploadWorker`)                            |
| **Navigation**          | Navigation Compose quản lý màn hình                                                  | `MainActivity.kt` (Class `MainActivity`, khối `NavHost`)                                    |
| **Content Provider**    | Truy cập danh bạ điện thoại (`ContactsContract`)                                     | `ContactsReader.kt` (Class `ContactsReader`, hàm `readContacts()`)                          |
| **Room Database**       | Lưu bản nháp và thông tin người dùng cục bộ                                          | `MDatabase.kt` (Class `MDatabase`, `DraftDao`, `UserDao`)                                   |
| **Web API** *(Bonus)*   | iTunes Search API tìm bài hát                                                        | `ItunesApiService.kt` (Hàm `searchSongs()`), `SongSearchDialog.kt`                          |
| **Animation** *(Bonus)* | `AnimatedVisibility`, `animateFloatAsState` (Scale hiệu ứng Camera)                  | `PendingPhotoConfirmationScreen.kt` (`AnimatedVisibility`), `CameraViewfinderPage.kt`       |
| **Firebase** *(Bonus)*  | Firebase Phone Auth, Firebase Realtime Database                                      | `AuthViewModel.kt` (`signInWithPhoneAuthCredential`), `FirebaseClientService.kt`            |

---

## Các chức năng cốt lõi và luồng hoạt động chi tiết

### 1. Đăng nhập & Xác thực (Authentication)
* **Luồng hoạt động chi tiết:**
  1. Người dùng mở ứng dụng và nhập số điện thoại tại màn hình đăng nhập.
  2. Ứng dụng gửi yêu cầu xác thực tới **Firebase Authentication**.
  3. Firebase gửi mã OTP dạng SMS tới số điện thoại của người dùng.
  4. Người dùng nhập mã OTP vào màn hình xác nhận.
  5. Nếu OTP hợp lệ, Firebase trả về chứng chỉ đăng nhập (Credential). Ứng dụng tự động lưu trữ trạng thái phiên đăng nhập.
  6. Ứng dụng kiểm tra người dùng đã tồn tại trên **Firebase Realtime Database** chưa. Nếu chưa, tạo mới User Profile (lưu UID, Số điện thoại) và đồng bộ thông tin về **Room Database** cục bộ.
  7. Điều hướng người dùng vào màn hình chính (`MainScreen`).

---

### 2. Tạo và Đăng Bài Viết (Create Post)
* **Luồng hoạt động chi tiết:**
  1. Từ giao diện máy ảnh, người dùng có thể tương tác:
     * **Chụp ảnh trực tiếp** bằng CameraX được nhúng ngay trên màn hình.
     * **Chọn ảnh từ thư viện** thông qua thành phần hệ thống `PickVisualMedia`.
  2. Ứng dụng chuyển sang trạng thái xác nhận ảnh. Tại đây, người dùng có thể nhập nội dung (caption).
  3. (Tuỳ chọn) Người dùng mở tính năng tìm nhạc. Ứng dụng gọi **iTunes Search API** để tìm kiếm bài hát và gắn kèm dữ liệu bài hát vào bài viết.
  4. **Lưu nháp tự động**: Bất kỳ thay đổi nào (ảnh, text, nhạc) đều tự động được ghi nhận vào **Room Database** dưới dạng bản nháp. Tính năng này giúp khôi phục nội dung nếu app vô tình bị đóng.
  5. Khi nhấn "Đăng", nếu kết nối mạng ổn định:
     * Ứng dụng tải ảnh lên hệ thống **Supabase Storage** và nhận về đường dẫn URL an toàn.
     * Thông tin bài viết (bao gồm Caption, ImageURL, SongData, UserID) được lưu lên **Firebase Realtime Database**.
     * Xoá bản nháp khỏi Room Database cục bộ sau khi đăng thành công.
  6. **Đăng bài chạy ngầm**: Hệ thống sử dụng **WorkManager** (`PostUploadWorker`) để duy trì tiến trình upload ảnh. Điều này giúp tiến trình đăng bài được xử lý ổn định, liên tục ở dưới nền ngay cả khi người dùng chuyển sang màn hình khác.

---

### 3. Tương Tác & Lướt Bảng Tin (New Feed)
* **Luồng hoạt động chi tiết:**
  1. Khi người dùng vào `MainScreen`, ứng dụng thực hiện truy xuất dữ liệu bài viết của bạn bè từ **Firebase Realtime Database**.
  2. Dữ liệu này được cache bởi cơ chế cục bộ của Firebase, đảm bảo trải nghiệm **hỗ trợ ngoại tuyến (Offline)** liền mạch khi mạng yếu hoặc không có mạng.
  3. Giao diện hiển thị danh sách bài viết theo thời gian thực gồm: ảnh, nội dung, bài hát đính kèm, tên nghệ sĩ, thông tin người đăng.
  4. Hình ảnh được tải hiển thị và tối ưu bằng **Coil Disk Cache** (giúp không tải lại ảnh đã xem).
  5. Người dùng tương tác (Like) bài viết. Hệ thống sẽ ngay lập tức đồng bộ thuộc tính `likeCount` lên Firebase theo thời gian thực.

---

### 4. Đồng Bộ & Gợi Ý Kết Bạn (Friends & Contacts)
* **Luồng hoạt động chi tiết:**
  1. Lần đầu truy cập, người dùng được yêu cầu cấp quyền danh bạ. Ứng dụng dùng `ContactsContract` để đọc danh sách số điện thoại liên lạc trong thiết bị.
  2. Danh sách số điện thoại này được mã hoá cơ bản và gửi lên **Firebase** để so khớp với tập dữ liệu người dùng trên hệ thống.
  3. Trả về và hiển thị danh sách "Có thể bạn biết" trên tab `FriendScreen`.
  4. Dữ liệu danh sách bạn bè được đồng bộ ngược về **Room Database** cục bộ để sử dụng ngoại tuyến.

---

### 5. Quản Lý Hồ Sơ Cá Nhân (Profile Management)
* **Luồng hoạt động chi tiết:**
  1. Người dùng chuyển sang tab `ProfileScreen`.
  2. Thông tin User (Tên hiển thị, Ảnh đại diện, Số điện thoại) được tải cực nhanh từ **Room Database** cục bộ để hiển thị, đồng thời lắng nghe mọi sự thay đổi từ **Firebase** để update theo thời gian thực.
  3. Người dùng cập nhật tên hiển thị. Dữ liệu được ghi nhận lên Firebase và phản hồi lại Room Database.
  4. Xem bài viết cá nhân đã đăng. Khi xoá bài viết, ứng dụng hiển thị AlertDialog xác nhận. Quá trình xoá thực hiện gỡ bỏ ảnh trên **Supabase** và record tương ứng trên **Firebase**.
  5. Đăng xuất: Ứng dụng gọi AlertDialog xác nhận. Nếu đồng ý, xoá sạch dữ liệu Room cục bộ, xoá chứng chỉ Firebase Authentication và điều hướng về trang đăng nhập.

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
| Background Task  | WorkManager                   | `PostUploadWorker.kt`                                                                       |
| Content Provider | ContactsContract              | `ContactsReader.kt`, `ContactProvider.kt`                                                   |
| Architecture     | MVVM                          | Kiến trúc chia tách rõ ràng 2 package `backend/domain` và `frontend/screens`                |

---

## Cấu trúc dữ liệu

### User

```text
phoneNumber
displayName
avatarUrl
```

### Post

```text
id
userId
imageUrl
caption
authorName
authorAvatar
songName
artistName
previewUrl
createdAt
likedBy
```

### DraftPost (Room)

```text
id
imageUri
caption
songName
artistName
artworkUrl
previewUrl
status
createdAt
```

### UserEntity (Room)

```text
phoneNumber
displayName
avatarUrl
isMe
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
        ├── users
        ├── posts
        └── friendships

Supabase Storage
        │
        ├── post
        └── avatars

Room
        │
        ├── UserEntity
        ├── FriendshipEntity
        ├── DraftEntity
        └── PhotoEntity

iTunes API
        │
        └── Song Search

Jetpack Compose
        │
        ▼
       UI
```
