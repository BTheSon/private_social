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
}