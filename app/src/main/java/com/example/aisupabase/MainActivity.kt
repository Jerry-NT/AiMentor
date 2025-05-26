package com.example.aisupabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aisupabase.screens.AdminHomeScreen
import com.example.aisupabase.screens.LoginScreen
import com.example.aisupabase.screens.RegisterScreen
import com.example.aisupabase.screens.ClientHomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController) }
                composable("admin") { AdminHomeScreen(navController) }
                composable("client") { ClientHomeScreen(navController) }
            }
        }
    }
}
