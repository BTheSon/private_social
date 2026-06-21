package com.locket.backend.domain.contact

import com.google.firebase.database.FirebaseDatabase
import com.locket.backend.domain.friend.FriendModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class ContactRepository(
    private val contactProvider: ContactProvider,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    companion object {
        // Số request gọi đồng thời tối đa lên Firebase trong 1 lô.
        private const val CHUNK_SIZE = 25
    }

    //cái đây để gợi ý bạn bè nè mà chưa dám thêm
    suspend fun getFriendSuggestions(
        myPhoneNumber: String,
        existingRelationNumbers: Set<String>
    ): List<FriendModel> {
        if (!contactProvider.hasPermission()) return emptyList()

        val deviceContacts = contactProvider.getDeviceContacts()
        if (deviceContacts.isEmpty()) return emptyList()

        // Danh sách ứng viên: loại bản thân + loại người đã có quan hệ + bỏ trùng
        val candidateNumbers = deviceContacts
            .map { it.normalizedPhoneNumber }
            .distinct()
            .filter { it != myPhoneNumber && it !in existingRelationNumbers }

        if (candidateNumbers.isEmpty()) return emptyList()

        // Map ngược lại để lấy tên trong danh bạ máy làm fallback nếu user chưa có displayName trên Firebase
        val contactNameMap = deviceContacts.associateBy(
            { it.normalizedPhoneNumber },
            { it.Name }
        )

        val usersRef = firebaseDatabase.getReference("users")
        val suggestions = mutableListOf<FriendModel>()

        // Chia thành từng lô để tránh quá tải, mỗi lô gọi song song
        candidateNumbers.chunked(CHUNK_SIZE).forEach { chunk ->
            val results = coroutineScope {
                chunk.map { phone ->
                    async {
                        phone to runCatching {
                            usersRef.child(phone).get().await()
                        }.getOrNull()
                    }
                }.map { it.await() }
            }

            for ((phone, snapshot) in results) {
                if (snapshot != null && snapshot.exists()) {
                    val appDisplayName = snapshot.child("displayName").getValue(String::class.java)

                    // FriendRowItem hiển thị avatar bằng chữ cái đầu của displayName,
                    // không dùng ảnh thật -> FriendModel không cần field avatarUrl.
                    suggestions.add(
                        FriendModel(
                            phoneNumber = phone,
                            displayName = appDisplayName ?: contactNameMap[phone] ?: phone,
                            relationStatus = "NONE"
                        )
                    )
                }
            }
        }

        return suggestions
    }

    // trong ContactRepository
    fun hasContactPermission(): Boolean = contactProvider.hasPermission()
}