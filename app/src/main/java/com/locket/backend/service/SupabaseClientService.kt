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
            supabaseUrl = "https://ggkvmrgoezkvprzaicjt.supabase.co",
            supabaseKey = "sb_publishable_IIrPdzcYr7s0nPBiXduzRw_53Tmi4Fk"
        ) {
            install(Auth)
            install(Storage)
            install(Postgrest)
        }
    }
}
