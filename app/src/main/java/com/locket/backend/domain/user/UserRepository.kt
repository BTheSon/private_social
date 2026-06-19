package com.locket.backend.domain.user

class UserRepository(private val userDao: UserDao) {

    suspend fun saveUser(user: UserEntity) {
        userDao.clearUserFlag()
        userDao.insertUser(user.copy(isMe = true))
    }

    suspend fun getMyProfile(): UserEntity? {
        return userDao.getUser()
    }

    fun getMyProfileFlow() = userDao.getUserFlow()

    suspend fun clearMyProfile() {
        userDao.clearUserFlag() // Hàm này đã được khai báo ở UserDao trong các bước trước
    }
}