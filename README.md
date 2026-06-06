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
Csdl của dự án sửa dụng json tree của realtime database với cấu trúc cơ bản như sau

```json
{
  "users": {
    "<phone_number>": {
      "displayName": "Bùi Thế Sơn",
      "avatarUrl": "https://firebasestorage...", 
      "fcmToken": "<token_de_gui_push_notification>" 
    }
  },

  "friendships": {
    "<my_phone_number>": {
      "<friend_phone_number>": {
        "status": "ACCEPTED",
        "senderPhone": "<my_phone_number>", 
        "timestamp": 1717650000
      }
    }
  },

  "photos": {
    "<photo_id>": {
      "senderPhone": "<phone_number>",
      "imageUrl": "https://firebasestorage...",
      "musicUrl": "https://firebasestorage...", 
      "description": "Cà phê sáng",
      "timestamp": 1717650000,
      "reactionCount": 5 
    }
  },

  "photo_reactions": {
    "<photo_id>": {
      "<friend_phone_number_1>": "❤️",
      "<friend_phone_number_2>": "😂"
    }
  },

  "user_feeds": {
    "<phone_number>": {
      "<photo_id>": 1717650000
    }
  },

  "chats": {
    "<chat_room_id>": {
      "<message_id>": {
        "senderPhone": "<phone_number>",
        "message": "Trời hôm nay đẹp quá!",
        "type": "text", 
        "replyPhotoId": "<photo_id>", 
        "timestamp": 1717650020
      }
    }
  }
}
```

Rằng buộc cho các thao tác với json tree
```json
{
  "rules": {
    "photos": {
      ".read": "auth != null",
      "$photo_id": {
        // Cho phép tạo mới.
        // Nếu sửa/xóa (dữ liệu đã tồn tại), senderPhone phải trùng với SĐT đang đăng nhập.
        ".write": "auth != null && (!data.exists() || data.child('senderPhone').val() === auth.uid)"
      }
    },

    "chats": {
      "$chat_room_id": {
        // Chỉ người có SĐT nằm trong ID phòng chat mới được đọc tin nhắn
        ".read": "auth != null && $chat_room_id.contains(auth.uid)",
        
        "$message_id": {
          // Tạo mới: senderPhone phải là SĐT đang đăng nhập
          // Sửa/Xóa: senderPhone cũ đã lưu phải là SĐT đang đăng nhập
          ".write": "auth != null && (
            (!data.exists() && newData.child('senderPhone').val() === auth.uid) 
            || 
            (data.exists() && data.child('senderPhone').val() === auth.uid)
          )"
        }
      }
    }
  }
}
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

* Thành viên 1: Bùi Thế Sơn
* Thành viên 2: Nguyễn Trần Thiên Bảo
* Thành viên 3: Phan Nguyễn Gia Huy