package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.ui.PhotoViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun CameraScreen(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // Dark locket background
    ) {
        if (cameraPermissionState.status.isGranted) {
            CameraContent(viewModel = viewModel)
        } else {
            PermissionPrompt(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
    }
}

@Composable
fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Yêu cầu quyền Camera",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Cần quyền máy ảnh",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Camera 1x1 yêu cầu quyền truy cập Camera của hệ thống để hiển thị kính ngắm và lưu giữ những bức ảnh vuông chất lượng cao cực chất.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
                .testTag("request_permission_button")
        ) {
            Text("CẤP QUYỀN CAMERA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

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

    // State machine for intermediate capture flows
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    // Vertical Pager scroll controller enabling snap swipe transitions between layout elements
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    // Resource optimization rule: CameraX only compiles when viewfinder screen is selected and no freeze framing preview is ongoing
    val isCameraActive = pagerState.currentPage == 0 && pendingPhotoFile == null

    // Delete photo trigger
    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = {
                Text(
                    text = "Xóa ảnh chụp",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa vĩnh viễn bức ảnh vuông này khỏi thiết bị không?",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        photoToDelete?.let { viewModel.deletePhoto(it) }
                        photoToDelete = null
                        Toast.makeText(context, "Đã xóa ảnh thành công", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("XÓA", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("HỦY", color = Color.White)
                }
            },
            containerColor = Color(0xFF222222),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal view trigger
    if (selectedPhotoForPreview != null) {
        AlertDialog(
            onDismissRequest = { selectedPhotoForPreview = null },
            title = null,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = selectedPhotoForPreview?.filePath,
                        contentDescription = "Full visual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPhotoForPreview = null }) {
                    Text("ĐÓNG", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vertical Pager housing screen pages
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // PAGE 0: Unified Camera Viewfinder and Physical controls
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F0F0F)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top bar layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFFFCC00), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LOCKET 1:1 LIVE",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "100% SECURE DIRECT",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Central Squircle Viewfinder aligned nicely
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(52.dp))
                                    .background(Color(0xFF191919))
                                    .border(
                                        width = 6.dp, 
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFFFCC00), Color(0xFFFF9100))
                                        ),
                                        shape = RoundedCornerShape(52.dp)
                                    )
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(46.dp))
                            ) {
                                if (isCameraActive) {
                                    CameraViewfinder(
                                        lensFacing = lensFacing,
                                        onImageCaptureReady = { cap -> imageCapture = cap },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Kính ngắm tạm nghỉ",
                                            color = Color.DarkGray,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Inner shading border inside the viewfinder block
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.2f)), shape = RoundedCornerShape(46.dp))
                                )
                            }
                        }

                        // Shutter & transition UI placed on bottom half of panel
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 96.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left button: History snapshot key or swipe button
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E1E1E))
                                        .border(2.dp, Color(0xFFFFCC00).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                        .combinedClickable(
                                            onClick = {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(1)
                                                }
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photos.isNotEmpty()) {
                                        AsyncImage(
                                            model = photos.first().filePath,
                                            contentDescription = "Xem lịch sử",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(14.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Kho ảnh trống",
                                            tint = Color.LightGray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // Middle button: Tactile Shutter trigger linking to intermediate file state
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (isCapturing) 0.82f else 1.0f,
                                    animationSpec = tween(durationMillis = 100),
                                    label = "ShutterScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .scale(animatedScale)
                                        .border(4.dp, Color.White, CircleShape)
                                        .padding(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isCapturing) Color.DarkGray else Color(0xFFFFCC00))
                                        .combinedClickable(
                                            onClick = {
                                                imageCapture?.let { cap ->
                                                    viewModel.captureToTemp(context, cap) { file ->
                                                        if (file != null) {
                                                            pendingPhotoFile = file
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Chụp hình thất bại, xin vui lòng thử lại!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                } ?: run {
                                                    Toast.makeText(
                                                        context,
                                                        "Vui lòng đợi camera khởi động!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )
                                        .testTag("camera_shutter_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCapturing) {
                                        CircularProgressIndicator(
                                            color = Color.Black,
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color.White.copy(alpha = 0.35f), CircleShape)
                                        )
                                    }
                                }

                                // Right button: Camera Lens toggling
                                IconButton(
                                    onClick = { viewModel.toggleLensFacing() },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color(0xFF1E1E1E), CircleShape)
                                        .border(1.dp, Color(0xFF333333), CircleShape)
                                        .testTag("camera_flip_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlipCameraAndroid,
                                        contentDescription = "Chuyển đổi camera",
                                        tint = Color(0xFFFFCC00),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Scroll navigation tip indicator layout
                            Row(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(1)
                                            }
                                        }
                                    )
                                    .padding(vertical = 4.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CUỘN LÊN XEM LỊCH SỬ KHOẢNH KHẮC",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // PAGE 1: Recent snapshot timeline feed
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F0F0F))
                    ) {
                        // Layout customized page bar header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(0)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0x1AFFCC00), CircleShape)
                                        .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Quay về máy ảnh",
                                        tint = Color(0xFFFFCC00),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "KHOẢNH KHẮC GẦN ĐÂY",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Nhật ký Locket cá nhân cực chất",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${photos.size} KHOẢNH KHẮC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFCC00),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)

                        if (photos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoAlbum,
                                        contentDescription = "Album trống",
                                        tint = Color(0xFF222222),
                                        modifier = Modifier.size(92.dp)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "Chưa có ảnh nào chụp",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Hãy chạm vào biểu tượng Máy ảnh góc trái để mở camera chụp và gửi khoảnh khắc của bạn ngay nhé!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                            ) {
                                items(photos, key = { it.id }) { photo ->
                                    TimelinePhotoItem(
                                        photo = photo,
                                        onClick = { selectedPhotoForPreview = photo },
                                        onLongClick = { photoToDelete = photo }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tactile Full-Screen Confirm and Post / Cancel Preview overlay
        AnimatedVisibility(
            visible = pendingPhotoFile != null,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F))
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title Header info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.statusBarsPadding().padding(top = 16.dp)
                    ) {
                        Text(
                            text = "XÁC NHẬN KHOẢNH KHẮC",
                            color = Color(0xFFFFCC00),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Xem trước ảnh 1x1 trước khi đăng tải",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Polaroid-style crop preview card frame containing captured photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(52.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(
                                width = 6.dp, 
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFCC00), Color(0xFFFF9100))
                                ),
                                shape = RoundedCornerShape(52.dp)
                            )
                            .padding(6.dp)
                            .clip(RoundedCornerShape(46.dp))
                    ) {
                        AsyncImage(
                            model = pendingPhotoFile,
                            contentDescription = "Chờ đăng tải",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Outer shadow bounding overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.2f)), shape = RoundedCornerShape(46.dp))
                        )
                    }

                    // Confirm and Reset interactive drawer row at bottom
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // CANCEL ACTION (Hủy cuộc chụp)
                            Button(
                                onClick = {
                                    if (pendingPhotoFile?.exists() == true) {
                                        pendingPhotoFile?.delete()
                                    }
                                    pendingPhotoFile = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF222222),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hủy bỏ ảnh chụp",
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HỦY BỎ",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // CONFIRM POST ACTION (Cropping square, saving and inserting)
                            Button(
                                onClick = {
                                    pendingPhotoFile?.let { tempFile ->
                                        viewModel.savePostFromTemp(context, tempFile) { success ->
                                            if (success) {
                                                Toast.makeText(
                                                    context,
                                                    "Cập nhật Locket thành công!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Lưu ảnh Locket thất bại!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            pendingPhotoFile = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFCC00),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Gửi khoảnh khắc này",
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GỬI ĐI",
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // Overlay progress spinner block during processing steps
                if (isCapturing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.82f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFCC00), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ĐANG ĐĂNG Lên LOCKET...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelinePhotoItem(
    photo: PhotoEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("timeline_photo_item_${photo.id}"),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main Polaroid Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp), // Leave space for the washi tape overlay
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF2C2C2C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // 1x1 Photo Frame resembling a sleek, thick-bordered Polaroid cutout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = photo.filePath,
                        contentDescription = "Timeline snapshot file",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Mini live badge overlay inside photo
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFFFFCC00), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LOCKET #${photo.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFCC00),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom strip containing timestamp details and quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gửi lúc • ${formatTimestamp(photo.timestamp).split(" ")[1]}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatTimestamp(photo.timestamp).split(" ")[0],
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    // Tactile delete action indicator
                    IconButton(
                        onClick = onLongClick,
                        modifier = Modifier
                            .background(Color(0xFF222222), CircleShape)
                            .size(36.dp)
                            .testTag("delete_button_${photo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Xóa ảnh",
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Giữ lâu trên ảnh để xóa khỏi bộ nhớ thiết bị",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }

        // Custom "Washi Tape" graphic element to anchor the polaroid sheet to the wall layout
        Box(
            modifier = Modifier
                .width(76.dp)
                .height(14.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xCCFFCC00),
                            Color(0xDDFFD54F),
                            Color(0xCCFFCC00)
                        )
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun CameraViewfinder(
    lensFacing: Int,
    onImageCaptureReady: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = suspendCancellableCoroutine<ProcessCameraProvider> { continuation ->
            cameraProviderFuture.addListener({
                try {
                    continuation.resume(cameraProviderFuture.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        cameraProvider.unbindAll()

        val preview = Preview.Builder().build()
        preview.surfaceProvider = previewView.surfaceProvider

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            Log.e("CameraViewfinder", "Use case binding failed", e)
        }
    }

    AndroidView(
        factory = {
            previewView.apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier
    )
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
