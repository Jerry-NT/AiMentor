package com.example.aisupabase.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.ui.theme.bg_card_admin
import com.example.aisupabase.ui.theme.icon_admin
import com.example.aisupabase.ui.theme.title_admin

// Data class cho menu items
data class MenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

// header section với gradient
@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4A90E2),
                        Color(0xFF7B68EE)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Admin Dashboard",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quản lý ứng dụng AI MENTOR",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

// Grid hiển thị các menu items
@Composable
private fun MenuGrid(items: List<MenuItem>,modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            MenuCard(
                item = item,
                onClick = item.onClick
            )
        }
    }
}

// Card hiển thị menu item
@Composable
private fun MenuCard(item: MenuItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bg_card_admin.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = icon_admin,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = title_admin,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

// render mặc định cho menu items - sự kiện chuyển trang
private fun defaultMenuItems(navController: NavController): List<MenuItem> {
    return listOf(
        MenuItem(
            id = "user_management",
            title = "Quản lý người dùng",
            icon = Icons.Default.AccountBox,
            onClick = { navController.navigate("admin_users") }
        ),
        MenuItem(
            id = "user_management_2",
            title = "Quản lý khóa học",
            icon = Icons.Default.Edit,
            onClick = { navController.navigate("admin_courses") }
        ),
        MenuItem(
            id = "user_management_3",
            title = "Quản lý bài học",
            icon = Icons.Default.Info,
            onClick = { navController.navigate("admin_lessons") }
        ),
        MenuItem(
            id = "user_management_4",
            title = "Quản lý Blogs",
            icon = Icons.Default.AccountBox,
            onClick = { navController.navigate("admin_blogs") }
        ),
        MenuItem(
            id = "user_management_4",
            title = "Quản lý loại Blogs",
            icon = Icons.Default.AccountBox,
            onClick = { navController.navigate("admin_tag_blogs") }
        ),
        MenuItem(
            id = "user_management_5",
            title = "Quản lý lộ trình",
            icon = Icons.Default.AccountBox,
            onClick = { navController.navigate("admin_roadmaps") }
        ),
        MenuItem(
            id = "user_management_6",
            title = "Quản lý Loại tài khoản",
            icon = Icons.Default.AccountBox,
            onClick = { navController.navigate("admin_type_accounts") }
        ),
        MenuItem(
            id = "user_management_8",
            title = "Quản lý Hóa đơn",
            icon = Icons.Default.AccountBox,
            onClick = { navController.navigate("admin_user_invoices") }
        )

    )
}

// hàm admin dashboard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    navController: NavController,
    modifier: Modifier = Modifier,
    menuItems: List<MenuItem> = defaultMenuItems(navController)
) {
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
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
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent) // Keep content transparent
            ) {
                HeaderSection()
                MenuGrid(
                    items = menuItems,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "admin") {
            authUser().clearUserSession(context)
            navController.navigate("login");
        }
    }

    AdminDashboard(navController)
}
