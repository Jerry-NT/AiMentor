package com.example.aisupabase.screens

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.aisupabase.config.SupabaseClientProvider

//  Main Activity
@Composable
fun ClientHomeScreen(
    navController: NavController
) {
    LaunchedEffect(Unit) {

    }

    val supabase = SupabaseClientProvider.client
}



