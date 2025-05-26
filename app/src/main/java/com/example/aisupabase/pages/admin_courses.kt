package com.example.aisupabase.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.aisupabase.config.SupabaseClientProvider

//  Main Activity
@Composable
fun Admin_Courses( navController: NavController) {
    // xử lý logic xác thực người dùng, kiểm tra quyền truy cập, v.v.
    LaunchedEffect(Unit) {

    }

    val supabase = SupabaseClientProvider.client
}