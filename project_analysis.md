# 📊 Phân tích dự án: Momently

## Cấu trúc thực tế

```
com.locket
├── backend/
│   ├── common/util/DateTimeUtils.kt
│   └── domain/
│       ├── auth/AuthViewModel.kt
│       ├── database/MDatabase.kt
│       ├── friend/ (FriendDao, FriendModel, FriendRepository, FriendshipEntity, FriendViewModel)
│       ├── photo/ (PhotoDao, PhotoEntity, PhotoRepository, PhotoViewModel)
│       ├── profile/ProfileViewModel.kt
│       └── user/ (UserDao, UserEntity, UserRepository)
└── frontend/screens/
    ├── auth/AuthScreen.kt
    ├── camera/ (CameraScreen, dialog, page/component)
    ├── friend/ (FriendScreen, FriendRowItem, ModifierExt)
    ├── gallery/GalleryScreen.kt
    ├── main/MainScreen.kt
    └── profile/ (ProfileScreen, dialog/LogoutDialog)
```

---

## ✅ Đã có (so với yêu cầu README)

| Yêu cầu | Trạng thái | File |
|---|---|---|
| Jetpack Compose UI | ✅ Đầy đủ | AuthScreen, CameraScreen, FriendScreen, ProfileScreen, GalleryScreen, MainScreen |
| Firebase Phone Auth | ✅ Có | AuthViewModel.kt |
| Navigation Compose | ✅ Có | MainScreen.kt |
| Room Database | ✅ Có | MDatabase, PhotoDao, UserDao, FriendDao + Entities |
| Bottom Navigation | ✅ Có | MainScreen.kt |
| Camera (CameraX) | ✅ Ngoài spec | CameraScreen — **không có trong README** |
| Gallery/Intent | ✅ Có | GalleryScreen.kt + Toast |
| AlertDialog | ✅ Một phần | LogoutDialog, DeletePhotoDialog |
| Friend suggestion (ContactsContract) | ✅ Một phần | FriendScreen, FriendViewModel |
| Profile + Delete post | ✅ Có | ProfileScreen, deletePhoto method |
| Animation (AnimatedVisibility) | ✅ Một phần | PendingPhotoConfirmationScreen, CameraViewfinderPage |
| Coil Compose | ✅ Có | CameraViewfinderPage |

---

## ❌ Thiếu (có trong README, không có trong code)

| Chức năng | README yêu cầu | Hiện trạng |
|---|---|---|
| **FeedScreen** | Màn hình xem bài viết bạn bè, Like | **Không tồn tại** — không có file FeedScreen.kt |
| **CreatePostScreen** | Đăng bài (caption + nhạc + upload) | **Không tồn tại** — không có file CreatePostScreen.kt |
| **PostDetailScreen** | Xem chi tiết bài viết | **Không tồn tại** — không có file PostDetailScreen.kt |
| **iTunes Search API** | Tìm bài hát | **Không có** — không có Retrofit/service tìm nhạc |
| **WorkManager** | Đồng bộ định kỳ bài viết & bạn bè | **Không có** — không có Worker class nào |
| **Draft (Bản nháp)** | Tự động lưu caption, URI ảnh, bài hát | **Không có** — Room entity chỉ lưu Photo/User/Friendship, không có DraftEntity |
| **Like bài viết** | Tương tác Like trên Feed | **Không có** logic like |
| **Supabase Storage upload** | Upload ảnh lên Supabase | **Không rõ** — PhotoViewModel có camera upload nhưng không thấy Supabase client |
| **AnimatedVisibility/Crossfade like** | Hiệu ứng Like | Animation chỉ có ở Camera flow, không ở Feed |
| **AlertDialog xác nhận xoá bài** | Dialog xoá post | Chỉ có LogoutDialog và DeletePhoto (ở Camera), chưa ở ProfileScreen |

---

## ⚠️ Dư / Không có trong README

| Thành phần | Hiện trạng | Ghi chú |
|---|---|---|
| **CameraScreen** (CameraX) | ✅ Có đầy đủ | Không đề cập trong README — đây là luồng chụp ảnh trực tiếp thay vì chọn từ gallery |
| **CameraViewfinderPage, PendingPhotoConfirmationScreen, TimelineHistoryPage, TimelinePhotoItem** | ✅ Có | Đây là UI camera phức tạp, ngoài spec |
| **GalleryScreen** | ✅ Có | README chỉ nói "mở Gallery", không phải screen riêng |
| **TimelineHistoryPage** | ✅ Có | Kiểu timeline lịch sử ảnh, không đề cập trong README |
| **FriendshipEntity** | ✅ Có | Lưu friendship trong Room, nhưng README chỉ yêu cầu gợi ý kết bạn |

---

## 📋 Kế hoạch thực thi (ưu tiên)

### 🔴 P1 — Màn hình cốt lõi (bắt buộc)

#### 1. `FeedScreen.kt`
- Hiển thị `LazyColumn` danh sách bài viết của bạn bè từ Firestore
- Mỗi item: `AsyncImage` ảnh, caption, tên bài hát, nghệ sĩ
- Nút Like với `AnimatedVisibility` / hiệu ứng heart
- Tích hợp vào `MainScreen` bottom nav tab

#### 2. `CreatePostScreen.kt`
- Nhận ảnh từ GalleryScreen (intent/URI)
- Nhập caption
- Tìm bài hát qua iTunes API
- Đính kèm bài hát
- Upload ảnh Supabase → lưu metadata Firestore
- Auto-save draft vào Room

#### 3. `PostDetailScreen.kt`
- Nhận `postId` từ navigation argument
- Hiển thị ảnh, caption, nhạc, số like
- Nếu là bài của mình: có nút xoá với `AlertDialog`

---

### 🟠 P2 — Backend/Service (quan trọng)

#### 4. iTunes Search API
```
network/
├── ItunesApiService.kt      (Retrofit interface)
├── ItunesApiClient.kt       (Retrofit instance)
└── model/SongModel.kt       (trackName, artistName, previewUrl)
```
- `PhotoViewModel` hoặc `CreatePostViewModel` gọi search

#### 5. WorkManager — Sync Worker
```
backend/worker/
└── SyncWorker.kt            (ListenableWorker)
```
- Đồng bộ bài viết bạn bè từ Firestore → Room định kỳ
- Đồng bộ danh sách bạn bè

#### 6. DraftEntity + DraftDao
```
backend/domain/draft/
├── DraftEntity.kt           (@Entity: id, caption, imageUri, songName, artistName)
├── DraftDao.kt              (insertDraft, getDraft, deleteDraft)
└── DraftRepository.kt
```
- Thêm vào `MDatabase`
- `CreatePostScreen` auto-save khi người dùng nhập

---

### 🟡 P3 — Cải thiện & hoàn thiện

#### 7. Supabase Storage Client
- Thêm Supabase SDK dependency
- `PhotoRepository` / `PhotoViewModel` upload ảnh thực sự
- Xác nhận flow: Camera/Gallery → Supabase URL → Firestore

#### 8. AlertDialog xoá bài ở ProfileScreen
- `DeletePostDialog.kt` trong `screens/profile/dialog/`
- Gọi từ `ProfileScreen` khi nhấn xoá bài

#### 9. ContactsContract hoàn chỉnh
- Kiểm tra `FriendViewModel` có đọc danh bạ thực không
- Nếu chỉ tìm user trong Firestore, cần thêm logic đọc số điện thoại từ máy

#### 10. AnimatedVisibility Like effect
- Thêm hiệu ứng Like vào `FeedScreen`
- `Crossfade` khi chuyển tab trong `MainScreen`

---

## Tóm tắt độ hoàn thiện

| Khu vực | % hoàn thiện |
|---|---|
| Authentication | ~90% |
| Camera flow | ~85% (ngoài spec) |
| Gallery | ~70% |
| Feed | **0%** |
| Create Post | **0%** |
| Post Detail | **0%** |
| Friends | ~60% |
| Profile | ~60% |
| iTunes API | **0%** |
| WorkManager | **0%** |
| Draft | **0%** |
| Supabase Upload | ~30% |
| Animation | ~40% |

**Tổng thể: ~40% hoàn thiện theo spec README**
