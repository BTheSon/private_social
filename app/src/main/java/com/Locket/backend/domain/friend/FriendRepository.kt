package com.Locket.backend.domain.friend

import com.Locket.backend.domain.user.UserEntity
import kotlinx.coroutines.flow.Flow

class FriendRepository(private val friendDao: FriendDao) {

    val friends: Flow<List<FriendModel>> = friendDao.getFriendsList()
    val receivedInvites: Flow<List<FriendModel>> = friendDao.getReceivedInvites()
    val sentInvites: Flow<List<FriendModel>> = friendDao.getSentInvites()
    val suggestions: Flow<List<FriendModel>> = friendDao.getSuggestions()

    fun searchUsers(query: String): Flow<List<FriendModel>> = friendDao.searchUsers(query)

    suspend fun sendFriendRequest(receiverPhone: String) {
        val myPhone = friendDao.getMyPhoneNumber() ?: return
        friendDao.insertFriendship(FriendshipEntity(senderPhone = myPhone, receiverPhone = receiverPhone, status = "PENDING"))
    }

    suspend fun acceptFriendRequest(senderPhone: String) {
        val myPhone = friendDao.getMyPhoneNumber() ?: return
        friendDao.insertFriendship(FriendshipEntity(senderPhone = senderPhone, receiverPhone = myPhone, status = "ACCEPTED"))
    }

    suspend fun cancelOrDeleteFriendship(targetPhone: String) {
        val myPhone = friendDao.getMyPhoneNumber() ?: return
        friendDao.deleteFriendship(myPhone, targetPhone)
    }

    // Hàm tạo Dữ liệu mẫu cực xịn để test chức năng ngay lập tức
    suspend fun prepopulateMockData() {
        // Tạo tài khoản của tôi trước
        friendDao.insertUser(UserEntity("0901234567", "Sơn Tùng M-TP", isMe = true))

        // Mock danh sách người dùng trong hệ thống
        val mockUsers = listOf(
            UserEntity("0988888888", "Bảo Đẹp Trai"),
            UserEntity("0911111111", "Anh Khang Kotlin"),
            UserEntity("0922222222", "Hải Phòng Dev"),
            UserEntity("0933333333", "Hà My Blazor"),
            UserEntity("0944444444", "Linh Chi Supabase"),
            UserEntity("0955555555", "JustaTee"),
            UserEntity("0966666666", "Soobin Hoàng Sơn"),
            UserEntity("0977777777", "Binz Da Poet")
        )
        mockUsers.forEach { friendDao.insertUser(it) }

        // Mối quan hệ mẫu
        // 1. Đã kết bạn
        friendDao.insertFriendship(FriendshipEntity("0901234567", "0988888888", "ACCEPTED"))
        friendDao.insertFriendship(FriendshipEntity("0911111111", "0901234567", "ACCEPTED"))

        // 2. Lời mời kết bạn ĐÃ NHẬN (Người khác gửi cho mình)
        friendDao.insertFriendship(FriendshipEntity("0922222222", "0901234567", "PENDING"))
        friendDao.insertFriendship(FriendshipEntity("0933333333", "0901234567", "PENDING"))

        // 3. Lời mời kết bạn ĐÃ GỬI (Mình đi gửi cho người khác)
        friendDao.insertFriendship(FriendshipEntity("0901234567", "0944444444", "PENDING"))
    }
}