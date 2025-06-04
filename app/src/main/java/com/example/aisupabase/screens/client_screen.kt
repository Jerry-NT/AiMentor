package com.example.aisupabase.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser

//  Main Activity
@Composable
fun ClientHomeScreen(navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "client") {
            authUser().clearUserSession(context)
            navController.navigate("login");
        }
    }

    val supabase = SupabaseClientProvider.client
    Button(
        onClick = {
            authUser().clearUserSession(context)
            navController.navigate("login") {
                popUpTo("admin_home") { inclusive = true }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
    ) {
        Text(
            text = "Đăng xuất",
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}



