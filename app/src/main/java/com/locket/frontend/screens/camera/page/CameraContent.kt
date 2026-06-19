package com.locket.frontend.screens.camera.page

import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.locket.backend.domain.photo.PhotoEntity
import com.locket.backend.domain.photo.PhotoViewModel
import com.locket.frontend.screens.camera.page.component.PendingPhotoConfirmationScreen
import com.locket.frontend.screens.camera.dialog.DeletePhotoDialog
import com.locket.frontend.screens.camera.dialog.PhotoPreviewDialog
import com.locket.frontend.screens.camera.page.component.CameraViewfinderPage
import com.locket.frontend.screens.camera.page.component.TimelineHistoryPage
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CameraContent(viewModel: PhotoViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photos by viewModel.allPhotos.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val lensFacing by viewModel.currentLensFacing.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var photoToDelete by remember { mutableStateOf<PhotoEntity?>(null) }
    var selectedPhotoForPreview by remember { mutableStateOf<PhotoEntity?>(null) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val isCameraActive = pagerState.currentPage == 0 && pendingPhotoFile == null

    // 1. Dialog Xóa Ảnh
    if (photoToDelete != null) {
        DeletePhotoDialog(
            onConfirm = {
                photoToDelete?.let { viewModel.deletePhoto(it) }
                photoToDelete = null
                Toast.makeText(context, "Đã xóa ảnh thành công", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { photoToDelete = null }
        )
    }

    // 2. Dialog Xem trước ảnh
    if (selectedPhotoForPreview != null) {
        PhotoPreviewDialog(
            filePath = selectedPhotoForPreview?.filePath,
            onDismiss = { selectedPhotoForPreview = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vertical Pager chứa các trang
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // 3. Trang Kính Ngắm (Camera)
                    CameraViewfinderPage(
                        isCameraActive = isCameraActive,
                        lensFacing = lensFacing,
                        firstPhotoPath = photos.firstOrNull()?.filePath,
                        isCapturing = isCapturing,
                        onImageCaptureReady = { cap -> imageCapture = cap },
                        onCaptureClick = {
                            imageCapture?.let { cap ->
                                viewModel.captureToTemp(context, cap) { file ->
                                    if (file != null) pendingPhotoFile = file
                                    else Toast.makeText(
                                        context,
                                        "Chụp hình thất bại!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } ?: Toast.makeText(
                                context,
                                "Vui lòng đợi camera khởi động!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSwitchLensClick = { viewModel.toggleLensFacing() },
                        onHistoryClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                    )
                }
                1 -> {
                    // 4. Trang Lịch Sử
                    TimelineHistoryPage(
                        photos = photos,
                        onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        onPhotoClick = { selectedPhotoForPreview = it },
                        onPhotoLongClick = { photoToDelete = it }
                    )
                }
            }
        }

        // 5. Màn hình xác nhận sau khi chụp
        PendingPhotoConfirmationScreen(
            pendingPhotoFile = pendingPhotoFile,
            isCapturing = isCapturing,
            onCancel = {
                if (pendingPhotoFile?.exists() == true) pendingPhotoFile?.delete()
                pendingPhotoFile = null
            },
            onConfirm = {
                pendingPhotoFile?.let { tempFile ->
                    viewModel.savePostFromTemp(context, tempFile) { success ->
                        if (success) Toast.makeText(
                            context,
                            "Cập nhật Locket thành công!",
                            Toast.LENGTH_SHORT
                        ).show()
                        else Toast.makeText(context, "Lưu ảnh Locket thất bại!", Toast.LENGTH_SHORT)
                            .show()
                        pendingPhotoFile = null
                    }
                }
            }
        )
    }
}