package com.locket.backend.service

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.locket.BuildConfig

object SupabaseClientService {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            // TODO: Bổ sung URL và Key thật vào BuildConfig hoặc .env file
            supabaseUrl = "YOUR_SUPABASE_URL",
            supabaseKey = "YOUR_SUPABASE_ANON_KEY"
        ) {
            install(Auth)
            install(Storage)
            install(Postgrest)
        }
    }
}
