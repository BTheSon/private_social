package com.locket.frontend.screens.camera.page

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.locket.backend.domain.music.SongModel
import com.locket.backend.domain.photo.PhotoEntity
import com.locket.backend.domain.photo.PhotoViewModel
import com.locket.backend.domain.post.PostModel
import com.locket.backend.domain.post.PostViewModel
import com.locket.frontend.screens.camera.dialog.DeletePhotoDialog
import com.locket.frontend.screens.camera.dialog.PhotoPreviewDialog
import com.locket.frontend.screens.camera.page.component.CameraViewfinderPage
import com.locket.frontend.screens.camera.page.component.PendingPhotoConfirmationScreen
import com.locket.frontend.screens.camera.page.component.TimelineHistoryPage
import com.locket.frontend.screens.post.SongSearchDialog
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CameraContent(
    photoViewModel: PhotoViewModel,
    postViewModel: PostViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photos by photoViewModel.allPhotos.collectAsState()
    val isCapturing by photoViewModel.isCapturing.collectAsState()
    val lensFacing by photoViewModel.currentLensFacing.collectAsState()
    val posts by postViewModel.posts.collectAsState()
    val musicSearchResults by postViewModel.searchResults.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var photoToDelete by remember { mutableStateOf<PhotoEntity?>(null) }
    var selectedPhotoForPreview by remember { mutableStateOf<PhotoEntity?>(null) }

    // State cho luồng xác nhận & đăng bài
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var pendingCaption by remember { mutableStateOf("") }
    var pendingSelectedSong by remember { mutableStateOf<SongModel?>(null) }
    var showMusicDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val isCameraActive = pagerState.currentPage == 0 && pendingPhotoFile == null

    // Gallery picker launcher — mở thư viện ảnh hệ thống
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            photoViewModel.saveUriToTempFile(context, selectedUri) { tempFile ->
                if (tempFile != null) {
                    pendingPhotoFile = tempFile
                    pendingCaption = ""
                    pendingSelectedSong = null
                } else {
                    Toast.makeText(context, "Không thể đọc ảnh từ thư viện!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 1. Dialog tìm kiếm nhạc (hiển thị overlay từ bất cứ đâu trong Camera flow)
    if (showMusicDialog) {
        SongSearchDialog(
            onDismissRequest = { showMusicDialog = false },
            onSongSelected = { song ->
                pendingSelectedSong = song
                showMusicDialog = false
            },
            searchSongs = { query -> postViewModel.searchMusic(query) },
            searchResults = musicSearchResults
        )
    }

    // 2. Dialog xóa ảnh local
    if (photoToDelete != null) {
        DeletePhotoDialog(
            onConfirm = {
                photoToDelete?.let { photoViewModel.deletePhoto(it) }
                photoToDelete = null
                Toast.makeText(context, "Đã xóa ảnh thành công", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { photoToDelete = null }
        )
    }

    // 3. Dialog xem trước ảnh local
    if (selectedPhotoForPreview != null) {
        PhotoPreviewDialog(
            filePath = selectedPhotoForPreview?.filePath,
            onDismiss = { selectedPhotoForPreview = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> {
                    CameraViewfinderPage(
                        isCameraActive = isCameraActive,
                        lensFacing = lensFacing,
                        isCapturing = isCapturing,
                        onImageCaptureReady = { cap -> imageCapture = cap },
                        onCaptureClick = {
                            imageCapture?.let { cap ->
                                photoViewModel.captureToTemp(context, cap) { file ->
                                    if (file != null) {
                                        pendingPhotoFile = file
                                        pendingCaption = ""
                                        pendingSelectedSong = null
                                    } else {
                                        Toast.makeText(context, "Chụp hình thất bại!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } ?: Toast.makeText(context, "Vui lòng đợi camera khởi động!", Toast.LENGTH_SHORT).show()
                        },
                        onSwitchLensClick = { photoViewModel.toggleLensFacing() },
                        onGalleryClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
                1 -> {
                    TimelineHistoryPage(
                        posts = posts,
                        onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                }
            }
        }

        // Màn hình xác nhận & đăng bài (overlay toàn màn hình)
        PendingPhotoConfirmationScreen(
            pendingPhotoFile = pendingPhotoFile,
            isCapturing = isCapturing,
            caption = pendingCaption,
            onCaptionChange = { pendingCaption = it },
            selectedSong = pendingSelectedSong,
            onAddMusicClick = { showMusicDialog = true },
            onRemoveSong = { pendingSelectedSong = null },
            onCancel = {
                if (pendingPhotoFile?.exists() == true) pendingPhotoFile?.delete()
                pendingPhotoFile = null
                pendingCaption = ""
                pendingSelectedSong = null
            },
            onConfirm = {
                pendingPhotoFile?.let { tempFile ->
                    photoViewModel.processAndPost(
                        context = context,
                        tempFile = tempFile,
                        caption = pendingCaption,
                        song = pendingSelectedSong
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, "Đăng khoảnh khắc thành công! 🎉", Toast.LENGTH_SHORT).show()
                            postViewModel.loadPosts() // refresh timeline
                        } else {
                            Toast.makeText(context, "Đăng thất bại, thử lại sau!", Toast.LENGTH_SHORT).show()
                        }
                        pendingPhotoFile = null
                        pendingCaption = ""
                        pendingSelectedSong = null
                    }
                }
            }
        )
    }
}