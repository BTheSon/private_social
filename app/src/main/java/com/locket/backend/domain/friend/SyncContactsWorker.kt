package com.locket.backend.domain.friend

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncContactsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val contactProvider = com.locket.backend.domain.contact.ContactProvider(applicationContext)
            if (!contactProvider.hasPermission()) return Result.failure()
            // Logic đồng bộ ngầm định kỳ
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
