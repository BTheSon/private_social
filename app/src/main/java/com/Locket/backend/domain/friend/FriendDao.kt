package com.Locket.backend.domain.friend

import androidx.room.*
import com.Locket.backend.domain.user.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: FriendshipEntity)

    @Query("DELETE FROM friendships WHERE (senderPhone = :p1 AND receiverPhone = :p2) OR (senderPhone = :p2 AND receiverPhone = :p1)")
    suspend fun deleteFriendship(p1: String, p2: String)

    // Lấy số điện thoại của chính người dùng hiện tại
    @Query("SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1")
    suspend fun getMyPhoneNumber(): String?

    // 1. Lấy danh sách bạn bè (Đã ACCEPTED)
    @Query("""
        SELECT u.phoneNumber, u.displayName, 'FRIEND' as relationStatus 
        FROM users u
        INNER JOIN friendships f ON (f.senderPhone = u.phoneNumber OR f.receiverPhone = u.phoneNumber)
        WHERE u.isMe = 0 AND f.status = 'ACCEPTED' 
        AND (f.senderPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1) 
             OR f.receiverPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1))
    """)
    fun getFriendsList(): Flow<List<FriendModel>>

    // 2. Lấy lời mời đã nhận (PENDING và mình là Người nhận)
    @Query("""
        SELECT u.phoneNumber, u.displayName, 'RECEIVED' as relationStatus 
        FROM users u
        INNER JOIN friendships f ON f.senderPhone = u.phoneNumber
        WHERE f.status = 'PENDING' 
        AND f.receiverPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1)
    """)
    fun getReceivedInvites(): Flow<List<FriendModel>>

    // 3. Lấy lời mời đã gửi (PENDING và mình là Người gửi)
    @Query("""
        SELECT u.phoneNumber, u.displayName, 'SENT' as relationStatus 
        FROM users u
        INNER JOIN friendships f ON f.receiverPhone = u.phoneNumber
        WHERE f.status = 'PENDING' 
        AND f.senderPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1)
    """)
    fun getSentInvites(): Flow<List<FriendModel>>

    // 4. Gợi ý kết bạn (Những người không có trong bảng mối quan hệ và không phải là mình)
    @Query("""
        SELECT u.phoneNumber, u.displayName, 'NONE' as relationStatus 
        FROM users u
        WHERE u.isMe = 0 
        AND u.phoneNumber NOT IN (
            SELECT senderPhone FROM friendships WHERE receiverPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1)
            UNION
            SELECT receiverPhone FROM friendships WHERE senderPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1)
        )
    """)
    fun getSuggestions(): Flow<List<FriendModel>>

    // Tìm kiếm người dùng theo tên hoặc số điện thoại (Không bao gồm bản thân)
    @Query("""
        SELECT u.phoneNumber, u.displayName, 
        CASE 
            WHEN f.status = 'ACCEPTED' THEN 'FRIEND'
            WHEN f.status = 'PENDING' AND f.senderPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1) THEN 'SENT'
            WHEN f.status = 'PENDING' AND f.receiverPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1) THEN 'RECEIVED'
            ELSE 'NONE'
        END as relationStatus
        FROM users u
        LEFT JOIN friendships f ON (
            (f.senderPhone = u.phoneNumber AND f.receiverPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1)) OR
            (f.receiverPhone = u.phoneNumber AND f.senderPhone = (SELECT phoneNumber FROM users WHERE isMe = 1 LIMIT 1))
        )
        WHERE u.isMe = 0 AND (u.displayName LIKE '%' || :searchQuery || '%' OR u.phoneNumber LIKE '%' || :searchQuery || '%')
    """)
    fun searchUsers(searchQuery: String): Flow<List<FriendModel>>
}