package com.locket.backend.domain.photo

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.locket.backend.domain.music.SongModel
import com.locket.backend.domain.post.PostModel
import com.locket.backend.domain.post.PostRepository
import com.locket.backend.service.FirebaseClientService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PhotoViewModel(
    application: Application,
    private val repository: PhotoRepository,
    private val postRepository: PostRepository
) : AndroidViewModel(application) {

    val allPhotos: StateFlow<List<PhotoEntity>> = repository.allPhotos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing = _isCapturing.asStateFlow()

    private val _currentLensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val currentLensFacing = _currentLensFacing.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab = _activeTab.asStateFlow()

    fun toggleLensFacing() {
        _currentLensFacing.value = if (_currentLensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun capturePhoto(context: Context, imageCapture: ImageCapture, onResult: (Boolean) -> Unit) {
        if (_isCapturing.value) return
        _isCapturing.value = true

        val cacheDir = context.cacheDir
        val tempFile = File(cacheDir, "temp_capture_${UUID.randomUUID()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val photosDir = File(context.filesDir, "captured_photos")
                            if (!photosDir.exists()) photosDir.mkdirs()
                            val finalFile = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")

                            val processed = processAndSaveSquare(tempFile, finalFile)
                            if (tempFile.exists()) tempFile.delete()

                            if (processed) {
                                repository.insertPhoto(PhotoEntity(filePath = finalFile.absolutePath))
                                _isCapturing.value = false
                                withContext(Dispatchers.Main) { onResult(true) }
                            } else {
                                _isCapturing.value = false
                                withContext(Dispatchers.Main) { onResult(false) }
                            }
                        } catch (e: Exception) {
                            Log.e("PhotoViewModel", "Error inside save coroutine", e)
                            _isCapturing.value = false
                            if (tempFile.exists()) tempFile.delete()
                            withContext(Dispatchers.Main) { onResult(false) }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("PhotoViewModel", "Camera Capture Error", exception)
                    _isCapturing.value = false
                    if (tempFile.exists()) tempFile.delete()
                    onResult(false)
                }
            }
        )
    }

    fun captureToTemp(context: Context, imageCapture: ImageCapture, onResult: (File?) -> Unit) {
        if (_isCapturing.value) return
        _isCapturing.value = true

        val cacheDir = context.cacheDir
        val tempFile = File(cacheDir, "temp_unprocessed_${UUID.randomUUID()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    _isCapturing.value = false
                    onResult(tempFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("PhotoViewModel", "Temporary Capture Error", exception)
                    _isCapturing.value = false
                    if (tempFile.exists()) tempFile.delete()
                    onResult(null)
                }
            }
        )
    }

    /**
     * Luồng đăng bài hoàn chỉnh:
     * 1. Crop ảnh tạm thành 1:1
     * 2. Upload lên Supabase Storage → lấy imageUrl
     * 3. Lưu PostModel lên Firebase Realtime Database
     * 4. Lưu ảnh vào Room DB local
     *
     * [isCapturing] được set true trong suốt quá trình để hiển thị loading overlay.
     */
    fun processAndPost(
        context: Context,
        tempFile: File,
        caption: String,
        song: SongModel?,
        onResult: (Boolean) -> Unit
    ) {
        _isCapturing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Crop 1:1
                val photosDir = File(context.filesDir, "captured_photos")
                if (!photosDir.exists()) photosDir.mkdirs()
                val processedFile = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
                val processed = processAndSaveSquare(tempFile, processedFile)

                if (tempFile.exists()) tempFile.delete()

                if (!processed) {
                    Log.e("PhotoViewModel", "processAndPost: processAndSaveSquare FAILED")
                    _isCapturing.value = false
                    withContext(Dispatchers.Main) { onResult(false) }
                    return@launch
                }
                Log.d("PhotoViewModel", "processAndPost: image processed OK → ${processedFile.absolutePath} (${processedFile.length()} bytes)")

                // 2. Upload Supabase Storage
                val bytes = processedFile.readBytes()
                Log.d("PhotoViewModel", "processAndPost: uploading ${bytes.size} bytes to Supabase...")
                val imageUrl = postRepository.uploadImageToSupabase(bytes)

                if (imageUrl.isNullOrEmpty()) {
                    Log.e("PhotoViewModel", "processAndPost: Supabase upload FAILED — imageUrl is null/empty")
                    // Vẫn lưu local dù cloud thất bại
                    repository.insertPhoto(PhotoEntity(filePath = processedFile.absolutePath))
                    _isCapturing.value = false
                    withContext(Dispatchers.Main) { onResult(false) }
                    return@launch
                }
                Log.d("PhotoViewModel", "processAndPost: Supabase upload OK → $imageUrl")

                // 3. Lưu Firebase RTDB
                val currentUserId = FirebaseClientService.auth.currentUser?.phoneNumber
                    ?: FirebaseClientService.auth.currentUser?.uid
                    ?: "unknown"

                val post = PostModel(
                    userId = currentUserId,
                    imageUrl = imageUrl,
                    caption = caption,
                    songName = song?.trackName,
                    artistName = song?.artistName,
                    previewUrl = song?.previewUrl
                )
                Log.d("PhotoViewModel", "processAndPost: saving post to Firebase RTDB as userId=$currentUserId...")
                val saved = postRepository.savePost(post)

                if (!saved) {
                    Log.e("PhotoViewModel", "processAndPost: Firebase RTDB savePost FAILED")
                }

                // 4. Lưu Room local
                repository.insertPhoto(PhotoEntity(filePath = processedFile.absolutePath))

                _isCapturing.value = false
                withContext(Dispatchers.Main) { onResult(saved) }
            } catch (e: Exception) {
                Log.e("PhotoViewModel", "processAndPost: EXCEPTION — ${e.javaClass.simpleName}: ${e.message}", e)
                _isCapturing.value = false
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun uriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "temp_gallery_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            Log.e("PhotoViewModel", "Error copying image from gallery", e)
            null
        }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePhoto(photo)
        }
    }

    private fun processAndSaveSquare(sourceFile: File, outputFile: File): Boolean {
        try {
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return false

            var rotationDegrees = 0
            try {
                val exif = ExifInterface(sourceFile.absolutePath)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } catch (e: Exception) {
                Log.e("PhotoViewModel", "ExifInterface failed", e)
            }

            val width = bitmap.width
            val height = bitmap.height
            val size = Math.min(width, height)
            val x = (width - size) / 2
            val y = (height - size) / 2

            val matrix = Matrix()
            if (rotationDegrees != 0) matrix.postRotate(rotationDegrees.toFloat())

            val squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size, matrix, true)

            outputFile.outputStream().use { out ->
                squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            if (squareBitmap != bitmap) squareBitmap.recycle()
            bitmap.recycle()
            return true
        } catch (e: Exception) {
            Log.e("PhotoViewModel", "Error during processAndSaveSquare", e)
            return false
        }
    }
}

class PhotoViewModelFactory(
    private val application: Application,
    private val repository: PhotoRepository,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoViewModel(application, repository, postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
