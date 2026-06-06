package com.Locket.backend.common.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateTimeUtils {

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

}