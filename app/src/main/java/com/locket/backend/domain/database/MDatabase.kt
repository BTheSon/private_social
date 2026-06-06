package com.locket.backend.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.locket.backend.domain.friend.FriendDao
import com.locket.backend.domain.friend.FriendshipEntity
import com.locket.backend.domain.photo.PhotoDao
import com.locket.backend.domain.photo.PhotoEntity
import com.locket.backend.domain.user.UserDao
import com.locket.backend.domain.user.UserEntity

@Database(entities = [PhotoEntity::class, UserEntity::class, FriendshipEntity::class], version = 2, exportSchema = false)
abstract class MDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun friendDao(): FriendDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: MDatabase? = null

        fun getDatabase(context: Context): MDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MDatabase::class.java,
                    "photo_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}