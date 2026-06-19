package com.locket.backend.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseClientService {
    // Mặc dù Firebase đã tự động khởi tạo bằng google-services.json,
    // việc gom các tham chiếu (reference) vào đây giúp dễ quản lý và mock khi test.

    val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    val database: FirebaseDatabase by lazy {
        Firebase.database
    }

    // Nếu bạn cài Firestore, có thể khai báo thêm:
    // val firestore: FirebaseFirestore by lazy { Firebase.firestore }
}
