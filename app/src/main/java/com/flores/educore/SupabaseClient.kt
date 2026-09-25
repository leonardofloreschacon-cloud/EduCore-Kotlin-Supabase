package com.flores.educore

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest


val supabase = createSupabaseClient(
    supabaseUrl = "https://hvwdaoxmoefyrputlfed.supabase.co",
    supabaseKey = "sb_publishable_5cdNhXoyXsHLeyVLtZyWYw_nMfa2qS1"
) {
    install(Postgrest)
    install(Auth)
}