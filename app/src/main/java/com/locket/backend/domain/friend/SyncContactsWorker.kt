package com.locket.backend.domain.friend

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncContactsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Logic đồng bộ ngầm định kỳ
        // Bạn của bạn sẽ điền code đọc danh bạ và gọi API gợi ý bạn bè ở đây
        return Result.success()
    }
}
