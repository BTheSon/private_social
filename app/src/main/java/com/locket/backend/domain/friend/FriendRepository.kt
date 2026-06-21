package com.locket.backend.domain.friend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // Lấy số điện thoại của chính mình từ Firebase Auth
    private val myPhone: String
        get() = auth.currentUser?.phoneNumber ?: ""

    // LẮNG NGHE BIẾN ĐỘNG BẠN BÈ THỜI GIAN THỰC TỪ FIREBASE
    fun observeFriendships(): Flow<List<FriendModel>> = callbackFlow {
        if (myPhone.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val friendshipRef = database.child("friendships").child(myPhone)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FriendModel>()
                viewModelScopeLaunch {
                    for (friendSnapshot in snapshot.children) {
                        val friendPhone = friendSnapshot.key ?: continue
                        val status = friendSnapshot.child("status").getValue(String::class.java) ?: "NONE"
                        val senderPhone = friendSnapshot.child("senderPhone").getValue(String::class.java) ?: ""

                        // Phân định chính xác trạng thái hiển thị cho UI dựa vào cấu trúc JSON của bạn
                        val relationStatus = when (status) {
                            "ACCEPTED" -> "FRIEND"
                            "PENDING" -> {
                                if (senderPhone == myPhone) "SENT" else "RECEIVED"
                            }
                            else -> "NONE"
                        }

                        // Lấy thêm displayName từ bảng users của đối phương để hiển thị
                        val userSnapshot = database.child("users").child(friendPhone).get().await()
                        val displayName = userSnapshot.child("displayName").getValue(String::class.java) ?: "User $friendPhone"

                        list.add(FriendModel(friendPhone, displayName, relationStatus))
                    }
                    trySend(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        friendshipRef.addValueEventListener(listener)
        // Hủy lắng nghe khi không còn dùng Flow này nữa để tránh rò rỉ bộ nhớ (Memory Leak)
        awaitClose { friendshipRef.removeEventListener(listener) }
    }

    // 1. GỬI LỜI MỜI KẾT BẠN (Ghi song song vào cả 2 tài khoản)
    suspend fun sendFriendRequest(friendPhone: String) {
        if (myPhone.isEmpty() || myPhone == friendPhone) return

        // --- LOGIC MỚI: Kiểm tra trạng thái hiện tại trên Firebase trước khi gửi ---
        val currentFriendship = database.child("friendships").child(myPhone).child(friendPhone).get().await()
        if (currentFriendship.exists()) {
            val status = currentFriendship.child("status").getValue(String::class.java)
            // Nếu ĐÃ LÀ BẠN hoặc ĐANG CÓ LỜI MỜI (Gửi/Nhận) thì tuyệt đối không cho gửi đè lệnh mới
            if (status == "ACCEPTED" || status == "PENDING") {
                return
            }
        }
        // --------------------------------------------------------------------------

        val timestamp = System.currentTimeMillis() / 1000
        val requestMap = mapOf(
            "status" to "PENDING",
            "senderPhone" to myPhone,
            "timestamp" to timestamp
        )

        val updates = hashMapOf<String, Any>(
            "/friendships/$myPhone/$friendPhone" to requestMap,
            "/friendships/$friendPhone/$myPhone" to requestMap
        )
        database.updateChildren(updates).await()
    }

    // 2. CHẤP NHẬN LỜI MỜI KẾT BẠN (Đổi trạng thái thành ACCEPTED ở cả 2 đầu)
    suspend fun acceptFriendRequest(friendPhone: String) {
        if (myPhone.isEmpty()) return

        val updates = hashMapOf<String, Any>(
            "/friendships/$myPhone/$friendPhone/status" to "ACCEPTED",
            "/friendships/$friendPhone/$myPhone/status" to "ACCEPTED"
        )
        database.updateChildren(updates).await()
    }

    // 3. HỦY BẠN / HỦY LỜI MỜI ĐÃ GỬI / TỪ CHỐI LỜI MỜI ĐÃ NHẬN
    suspend fun removeFriendship(friendPhone: String) {
        if (myPhone.isEmpty()) return

        val updates = hashMapOf<String, Any?>(
            "/friendships/$myPhone/$friendPhone" to null,
            "/friendships/$friendPhone/$myPhone" to null
        )
        database.updateChildren(updates).await()
    }

    // Tìm kiếm một user bất kỳ trên Firebase theo SĐT (Dùng cho Dialog)
    suspend fun findUserOnFirebase(phone: String): FriendModel? {
        val formattedPhone = if (phone.startsWith("0")) "+84${phone.substring(1)}" else phone
        if (formattedPhone == myPhone) return null // Không tự tìm chính mình

        return try {
            val userSnapshot = database.child("users").child(formattedPhone).get().await()
            if (userSnapshot.exists()) {
                val displayName = userSnapshot.child("displayName").getValue(String::class.java) ?: "Locket User"

                // Kiểm tra xem hiện tại đã có mối quan hệ nào chưa
                val friendshipSnapshot = database.child("friendships").child(myPhone).child(formattedPhone).get().await()
                val relationStatus = if (friendshipSnapshot.exists()) {
                    val status = friendshipSnapshot.child("status").getValue(String::class.java) ?: "NONE"
                    val senderPhone = friendshipSnapshot.child("senderPhone").getValue(String::class.java) ?: ""
                    if (status == "ACCEPTED") "FRIEND" else if (senderPhone == myPhone) "SENT" else "RECEIVED"
                } else {
                    "NONE"
                }

                FriendModel(formattedPhone, displayName, relationStatus)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

// Hàm Helper để hỗ trợ coroutine scope chạy trong listener NoSQL
private fun viewModelScopeLaunch(block: suspend () -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) { block() }
}