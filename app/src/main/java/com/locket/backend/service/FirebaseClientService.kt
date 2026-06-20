package com.locket.backend.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseClientService {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
}
