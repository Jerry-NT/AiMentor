package com.example.aisupabase.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

object bottombar{
    @Composable
    fun BottomNavigationBar(
        selectedIndex: Int,
        onItemSelected: (Int) -> Unit,
        navController: NavController
    ) {
        val items = listOf(
            Triple("Trang chủ", Icons.Default.Home, "client_home"),
            Triple("Khóa học", Icons.Default.AccountBox, "client_course"),
            Triple("Tìm kiếm", Icons.Default.Search, "client_search"),
            Triple("Blogs", Icons.Default.Edit, "client_blog"),
            Triple("Cài đặt", Icons.Default.Settings, "client_profile")
        )

        NavigationBar {
            items.forEachIndexed { index, (title, icon,route) ->
                NavigationBarItem(
                    icon = { Icon(imageVector = icon, contentDescription = title) },
                    label = { Text(text = title, fontSize = 10.sp) },
                    selected = index == selectedIndex,
                    onClick = {
                        onItemSelected(index)
                        navController.navigate(route)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4ECDC4),
                        selectedTextColor = Color(0xFF4ECDC4),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                    )
                )
            }
        }
    }

}