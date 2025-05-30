package com.example.aisupabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aisupabase.pages.Admin_Blogs
import com.example.aisupabase.pages.Admin_Courses
import com.example.aisupabase.pages.Admin_Lessons
import com.example.aisupabase.pages.Admin_Questions
import com.example.aisupabase.pages.Admin_Roadmaps
import com.example.aisupabase.pages.Admin_Tag_Blogs
import com.example.aisupabase.pages.Admin_Type_Accounts
import com.example.aisupabase.pages.Admin_User_Invoids
import com.example.aisupabase.pages.Admin_Users
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

                composable("admin_home") { AdminHomeScreen(navController) }
                composable("admin_blogs") { Admin_Blogs(navController) }
                composable("admin_tag_blogs") { Admin_Tag_Blogs(navController) }
                composable("admin_courses") { Admin_Courses(navController) }
                composable("admin_lessons") { Admin_Lessons(navController) }
                composable("admin_roadmaps") { Admin_Roadmaps(navController) }
                composable("admin_user_invoices") { Admin_User_Invoids(navController) }
                composable("admin_users") { Admin_Users(navController) }
                composable("admin_type_accounts") { Admin_Type_Accounts(navController) }
                composable("admin_question") { Admin_Questions(navController) }

                composable("client_home") { ClientHomeScreen(navController) }
            }


        }
    }
}
