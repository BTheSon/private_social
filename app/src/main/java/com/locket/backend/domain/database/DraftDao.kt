package com.locket.backend.domain.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts WHERE id = 1 LIMIT 1")
    suspend fun getDraft(): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftEntity)

    @Query("DELETE FROM drafts WHERE id = 1")
    suspend fun deleteDraft()
}
