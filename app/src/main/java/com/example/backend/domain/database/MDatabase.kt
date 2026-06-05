package com.example.backend.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.backend.domain.friend.FriendDao
import com.example.backend.domain.friend.FriendshipEntity
import com.example.backend.domain.photo.PhotoDao
import com.example.backend.domain.photo.PhotoEntity
import com.example.backend.domain.user.UserEntity

@Database(entities = [PhotoEntity::class, UserEntity::class, FriendshipEntity::class], version = 1, exportSchema = false)
abstract class MDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun friendDao(): FriendDao

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
                    .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}